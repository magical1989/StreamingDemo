package com.liuxi.spark.base.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class ESHelper implements Closeable {
    private Logger logger = Logger.getLogger(ESHelper.class);

    private TransportClient es_client;
    private String hosts;
    private String clusterName;

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void init() throws ESConnectException {
        logger.info("Create ESHelper | GUID = " + UUID.randomUUID().toString());

        if (hosts == null || hosts.isEmpty())
            throw new ESConnectException("es hosts 没有配置");
        if (clusterName == null || hosts.isEmpty())
            throw new ESConnectException("es clusterName 没有配置");

        try {
            Settings settings = Settings.builder()
                    .put("cluster.name", this.clusterName)
                    .put("client.transport.ping_timeout", 500, TimeUnit.MILLISECONDS)
                    .build();

            es_client = TransportClient.builder().settings(settings).build();

            if (StringUtils.isEmpty(hosts)) {
                es_client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300));
            } else {
                String[] hostArray = StringUtils.split(hosts, ";");
                for (String host : hostArray) {
                    String[] info = StringUtils.split(host, ":");
                    es_client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(info[0]), Integer.parseInt(info[1])));
                }
            }
        } catch (Exception e) {
            throw new ESConnectException("ES连接异常," + e.getMessage() + "；配置信息：hosts:" + hosts + "，clusterName:" + clusterName);
        }
    }


    public void executeBulkUpsert(final String indiceName, final String type, Map<String, Map<String, Object>> maps) {
        if (!maps.isEmpty()) {
            try {
                BulkRequest req = new BulkRequest();
                for (Map.Entry<String, Map<String, Object>> map : maps.entrySet()) {
                    XContentBuilder source = XContentFactory.jsonBuilder().startObject();
                    for (Map.Entry<String, Object> entry : map.getValue().entrySet()) {
                        source.field(entry.getKey(), entry.getValue());
                    }
                    source.endObject();
                    // 设置查询条件, 查找不到则添加生效
                    IndexRequest indexRequest = new IndexRequest(indiceName, type, map.getKey()).source(source);
                    // 设置更新, 查找到更新下面的设置
                    UpdateRequest upsert = new UpdateRequest(indiceName, type, map.getKey()).doc(source).upsert(indexRequest);
                    req.add(upsert);
                }
                es_client.bulk(req).actionGet();
            } catch (IOException e) {

            } catch (ElasticsearchException e) {

            }
        }
    }

    @Override
    public void close() throws IOException {
        if (es_client != null) {
            es_client.close();
        }
    }
}
