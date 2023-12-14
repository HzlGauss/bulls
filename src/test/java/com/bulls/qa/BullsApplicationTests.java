package com.bulls.qa;


//@SpringBootTest
//@ContextConfiguration(classes = SConfig.class)
//class QuantumApplicationTests  extends AbstractTestNGSpringContextTests {
//
//    @Autowired
//    QiHoOrderService qiHoOrderService;
//
//
//    @Autowired
//    SqlUtils sqlUtils;
//
//
//
//    @Test
//    void contextLoads() {
//		qiHoOrderService.batchInsertOrder(100,QiHoEnum.TO_SEND,QiHoEnum.ORDERID);
//        List<String> list = new ArrayList<>();
//        list.add("123456");
//        list.add("987656,11232131");
//        qiHoOrderService.createUploadFile("data/测试.xlsx", list, QiHoEnum.CONFIRM);
//        qiHoOrderService.deleteFile("data/测试.xlsx");

//        Map<String, Object> parameters = new HashMap<>();
//        Request request = null;
//
//        parameters.put("startTime", SimpleDateUtils.getThisDayBegin(0, "yyyy/MM/dd HH:mm:ss"));
//        parameters.put("endTime", SimpleDateUtils.getThisDayEnd(0, "yyyy/MM/dd HH:mm:ss"));
//        parameters.put("pageSize", 40);
//        request = Request.getInstance("shopOrderOrderPage");
//        Response response = request.setParameters(parameters).doRequest();

//        String deployId = "52";
//        String path = ":7797/kuaidi/push";
//        Map<String, Object> parameters = new HashMap<>();
//        parameters.put("com", "ems");
//        parameters.put("postId", "ems202007030001");
//        parameters.put("state", "3");
//        parameters.put("condition", "1");
//
//        qiHoOrderService.runJob(deployId, path, parameters);
//        String sql="SELECT * FROM tb_qiho_order order by gmt_create desc LIMIT 20";
//        System.out.println(sqlUtils.QuerySQL(sql));
//
//    }
//
//}
