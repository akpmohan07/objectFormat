package com.learning;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

/**
 * Hello world!
 *
 */
public class App
{
    static JsonObject formatJson(JsonObject data){
        JsonObject metaData ;
        JsonObject out = new JsonObject();
        JsonObject payload = new JsonObject();
        JsonObject vm = new JsonObject();
        JsonObject cp = new JsonObject();
        JsonObject pp = new JsonObject();

        metaData =  data.getJsonObject("metadata");
        metaData.remove("user-credentails");


        JsonObject source =  data.getJsonObject("payload").getJsonArray("vm").getJsonObject(0);
        cp.put("InstanceId",source.getString("InstanceId"));
        cp.put("InstanceType",source.getString("InstanceType"));
        cp.put("AvailabilityZone",source.getJsonObject("Placement").getString("AvailabilityZone"));
        cp.put("State",source.getJsonObject("State").getString("Name"));
        cp.put("privateIpAddress",source.getJsonArray("NetworkInterfaces").getJsonObject(0).getJsonArray("PrivateIpAddresses").getJsonObject(0).getString("PrivateIpAddress"));
        cp.put("Tags",source.getString("Tags"));

        pp.put("AmiLaunchIndex",source.getString("AmiLaunchIndex"));
        pp.put("ImageId",source.getString("ImageId"));
        pp.put("KeyName",source.getString("KeyName"));
        pp.put("LaunchTime",source.getString("LaunchTime"));
        pp.put("monitoringState",source.getJsonObject("Monitoring").getString("State"));
        pp.put("placementGroupName",source.getJsonObject("Placement").getString("GroupName"));
        pp.put("placementTenancy",source.getJsonObject("Placement").getString("Tenancy"));
        pp.put("PrivateDnsName",source.getString("PrivateDnsName"));
        pp.put("PublicDnsName",source.getString("PublicDnsName"));
        pp.put("SubnetId",source.getString("SubnetId"));
        pp.put("VpcId",source.getString("VpcId"));
        pp.put("EbsOptimized",source.getString("EbsOptimized"));
        pp.put("Hypervisor",source.getString("Hypervisor"));

        System.out.println(source.getJsonArray("SecurityGroups").size());
        JsonArray securityGroups = new JsonArray();
        for(int i=0;i<source.getJsonArray("SecurityGroups").size();i++){
            securityGroups.add(source.getJsonArray("SecurityGroups").getJsonObject(0).getString("GroupId"));
        }
        pp.put("SecurityGroups",securityGroups);


        vm.put("common-properties",cp);
        vm.put("provider-properties",pp);

        payload.put("vm",vm);

        out.put("metadata",metaData);
        out.put("payload",payload);

        return out;

    }
    public static void main( String[] args )
    {
        String jsonString = "{\"metadata\":{\"origin\":\"\",\"category\":\"cloud_discovery/saas_discovery\",\"sub-category\":\"aws-ec2\",\"actions\":[\"list-vm\",\"\"],\"trace-id\":\"\",\"started-at\":\"\",\"account-id\":\"\",\"org-id\":\"\",\"user-credentails\":{}},\"payload\":{\"vm\":[{\"AmiLaunchIndex\":0,\"ImageId\":\"ami-0abcdef1234567890\",\"InstanceId\":\"i-1234567890abcdef0\",\"InstanceType\":\"t2.micro\",\"KeyName\":\"MyKeyPair\",\"LaunchTime\":\"2018-05-10T08:05:20.000Z\",\"Monitoring\":{\"State\":\"disabled\"},\"Placement\":{\"AvailabilityZone\":\"us-east-2a\",\"GroupName\":\"\",\"Tenancy\":\"default\"},\"PrivateDnsName\":\"ip-10-0-0-157.us-east-2.compute.internal\",\"PrivateIpAddress\":\"10.0.0.157\",\"ProductCodes\":[],\"PublicDnsName\":\"\",\"State\":{\"Code\":0,\"Name\":\"pending\"},\"StateTransitionReason\":\"\",\"SubnetId\":\"subnet-04a636d18e83cfacb\",\"VpcId\":\"vpc-1234567890abcdef0\",\"Architecture\":\"x86_64\",\"BlockDeviceMappings\":[],\"ClientToken\":\"\",\"EbsOptimized\":false,\"Hypervisor\":\"xen\",\"NetworkInterfaces\":[{\"Attachment\":{\"AttachTime\":\"2018-05-10T08:05:20.000Z\",\"AttachmentId\":\"eni-attach-0e325c07e928a0405\",\"DeleteOnTermination\":true,\"DeviceIndex\":0,\"Status\":\"attaching\"},\"Description\":\"\",\"Groups\":[{\"GroupName\":\"MySecurityGroup\",\"GroupId\":\"sg-0598c7d356eba48d7\"}],\"Ipv6Addresses\":[],\"MacAddress\":\"0a:ab:58:e0:67:e2\",\"NetworkInterfaceId\":\"eni-0c0a29997760baee7\",\"OwnerId\":\"123456789012\",\"PrivateDnsName\":\"ip-10-0-0-157.us-east-2.compute.internal\",\"PrivateIpAddress\":\"10.0.0.157\",\"PrivateIpAddresses\":[{\"Primary\":true,\"PrivateDnsName\":\"ip-10-0-0-157.us-east-2.compute.internal\",\"PrivateIpAddress\":\"10.0.0.157\"}],\"SourceDestCheck\":true,\"Status\":\"in-use\",\"SubnetId\":\"subnet-04a636d18e83cfacb\",\"VpcId\":\"vpc-1234567890abcdef0\",\"InterfaceType\":\"interface\"}],\"RootDeviceName\":\"/dev/xvda\",\"RootDeviceType\":\"ebs\",\"SecurityGroups\":[{\"GroupName\":\"MySecurityGroup\",\"GroupId\":\"sg-0598c7d356eba48d7\"}],\"SourceDestCheck\":true,\"StateReason\":{\"Code\":\"pending\",\"Message\":\"pending\"},\"Tags\":[],\"VirtualizationType\":\"hvm\",\"CpuOptions\":{\"CoreCount\":1,\"ThreadsPerCore\":1},\"CapacityReservationSpecification\":{\"CapacityReservationPreference\":\"open\"},\"MetadataOptions\":{\"State\":\"pending\",\"HttpTokens\":\"optional\",\"HttpPutResponseHopLimit\":1,\"HttpEndpoint\":\"enabled\"}}]}}";
        JsonObject data = new JsonObject(jsonString);
        Vertx vertx = Vertx.vertx();
        HttpServer httpserver =  vertx.createHttpServer();
        EventBus eb = vertx.eventBus();
        MessageConsumer<String> consumer = eb.consumer("com.start");
        consumer.handler(message -> {
            if(message.body() == "start"){
                System.out.println("Process started..");
                System.out.println(formatJson(data));

            }
        });

        Router router = Router.router(vertx);
        router
                .route("/start")
                .handler(routingContext -> {
                    HttpServerResponse response = routingContext.response();
                    response.putHeader("content-type","text/plain");
                    eb.publish("com.start","start");
                    response.end("Process Started!");
                });
        httpserver
                .requestHandler(router)
                .listen(1234);
       // System.out.println( "Hello World!"+vertx+"\n"+router+"\n"+httpserver);
    }
}
