//use with:    mongo test --eval "var arg1=10000; arg2=1" randomDocument.js

function generateUUID(){
  var d = new Date().getTime();
  var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    var r = (d + Math.random()*16)%16 | 0;
    d = Math.floor(d/16);
    return (c=='x' ? r : (r&0x3|0x8)).toString(16);
  });
  return uuid;
};

function getGroupUrl(group, listGroup){
  if(group === listGroup[0] || group === listGroup[1])
    return "universal-programmer";
  if(group === listGroup[2] || group === listGroup[3] || group === listGroup[4] || group === listGroup[5] || group === listGroup[6])
    return "8051-usb-programmer";
  if(group === listGroup[7] || group === listGroup[8])
    return "raspberry-pi";
  if(group === listGroup[9] || group === listGroup[10])
    return "arduino-shield";
  if(group === listGroup[11] || group === listGroup[12])
    return "gyroscopes";
  if(group === listGroup[13] || group === listGroup[14])
    return "light-sensor";
  if(group === listGroup[15] )
    return "memory";
  if(group === listGroup[16] )
    return "24cxx-serial-eeprom";
  if(group === listGroup[17] )
    return "led-1w";
  if(group === listGroup[18] )
    return "led-smd-0805";
  if(group === listGroup[19] )
    return "character-lcd";
   if(group === listGroup[20] || group === listGroup[21])
    return "graphic-lcd";
}

function getSubType(group, listGroup){
  if(group === listGroup[0] || group === listGroup[1])
    return "Máy Nạp & Adapter";
  if(group === listGroup[2] || group === listGroup[3] || group === listGroup[4] || group === listGroup[5] || group === listGroup[6])
    return "Máy Nạp & Adapter";
  if(group === listGroup[7] || group === listGroup[8])
    return "Board Phát triển";
  if(group === listGroup[9] || group === listGroup[10])
    return "Board Phát triển";
  if(group === listGroup[11] || group === listGroup[12])
    return "Sensors, Transducers";
  if(group === listGroup[13] || group === listGroup[14])
    return "Sensors, Transducers";
  if(group === listGroup[15] )
    return "Memory ICs";
  if(group === listGroup[16] )
    return "Memory ICs";
  if(group === listGroup[17] )
    return "LEDs";
  if(group === listGroup[18] )
    return "LEDs";
  if(group === listGroup[19] )
    return "LCDs Display";
  if(group === listGroup[20] || group === listGroup[21])
    return "LCDs Display";
}

function getSubTypeUrl(group, listGroup){
  if(group === listGroup[0] || group === listGroup[1])
    return "may-nap-adapter";
  if(group === listGroup[2] || group === listGroup[3] || group === listGroup[4] || group === listGroup[5] || group === listGroup[6])
    return "may-nap-adapter";
  if(group === listGroup[7] || group === listGroup[8])
    return "board-phat-trien";
  if(group === listGroup[9] || group === listGroup[10])
    return "board-phat-trien";
  if(group === listGroup[11] || group === listGroup[12])
    return "sensor-transducer";
  if(group === listGroup[13] || group === listGroup[14])
    return "sensor-transducer";
  if(group === listGroup[15] )
    return "memory-ic";
  if(group === listGroup[16] )
    return "memory-ic";
  if(group === listGroup[17] )
    return "led";
  if(group === listGroup[18] )
    return "led";
  if(group === listGroup[19] )
    return "lcd-display";
  if(group === listGroup[20] || group === listGroup[21])
    return "lcd-display";
}

function getSupType (group, listGroup){
  if(group === listGroup[0] || group === listGroup[1])
    return "Phần cứng, Thiết bị";
  if(group === listGroup[2] || group === listGroup[3] || group === listGroup[4] || group === listGroup[5] || group === listGroup[6])
    return "Phần cứng, Thiết bị";
  if(group === listGroup[7] || group === listGroup[8])
    return "Phần cứng, Thiết bị";
  if(group === listGroup[9] || group === listGroup[10])
    return "Phần cứng, Thiết bị";
  if(group === listGroup[11] || group === listGroup[12])
    return "LK Bán dẫn & Cảm biến";
  if(group === listGroup[13] || group === listGroup[14])
    return "LK Bán dẫn & Cảm biến";
  if(group === listGroup[15] )
    return "LK Bán dẫn & Cảm biến";
  if(group === listGroup[16] )
    return "LK Bán dẫn & Cảm biến";
  if(group === listGroup[17] )
    return "LK Khác và Phụ kiện";
  if(group === listGroup[18] )
    return "LK Khác và Phụ kiện";
  if(group === listGroup[19] )
    return "LK Khác và Phụ kiện";
  if(group === listGroup[20] || group === listGroup[21])
    return "LK Khác và Phụ kiện";
}

function getSupTypeUrl (group, listGroup){
  if(group === listGroup[0] || group === listGroup[1])
    return "phan-cung-thiet-bi";
  if(group === listGroup[2] || group === listGroup[3] || group === listGroup[4] || group === listGroup[5] || group === listGroup[6])
    return "phan-cung-thiet-bi";
  if(group === listGroup[7] || group === listGroup[8])
    return "phan-cung-thiet-bi";
  if(group === listGroup[9] || group === listGroup[10])
    return "phan-cung-thiet-bi";
  if(group === listGroup[11] || group === listGroup[12])
    return "ban-dan-cam-bien";
  if(group === listGroup[13] || group === listGroup[14])
    return "ban-dan-cam-bien";
  if(group === listGroup[15] )
    return "ban-dan-cam-bien";
  if(group === listGroup[16] )
    return "ban-dan-cam-bien";
  if(group === listGroup[17] )
    return "lk-khac-phu-kien";
  if(group === listGroup[18] )
    return "lk-khac-phu-kien";
  if(group === listGroup[19] )
    return "lk-khac-phu-kien";
  if(group === listGroup[20] || group === listGroup[21])
    return "lk-khac-phu-kien";
}

var listGroup =
    [
//Phần cứng, Thiết bị
    //Máy Nạp & Adapter
       "Universal Programmer",
       "USB Programmer",

       "Firmware Master Chip",
       "ISP Programmer",
       "ISP Programmer / Emulator",
       "ISP/Parallel Programmer Mode",
       "Parallel Programmer mode",

    //Board Phát triển
        "mini PC",
        "Raspberry Pi Case",

        "Arduino",
        "Arduino Shield",

//LK Bán dẫn & Cảm biến
    //Sensors, Transducers
        "Accelerometer and Gyro Breakout",
        "Gyroscope sensor",

        "Light Sensor",
        "Sensors",

    //Memory ICs
        "Memory",

        "Memory-I2C Serial EEPROM",

//LK Khác và Phụ kiện
    //LEDs
        "LED 1W",

        "LEDs",

    //LCDs Display
        "Graphic LEDs",

        "Graphic LCDs",
        "LCDs"
    ];


var minDate = new Date(2012, 0, 1, 0, 0, 0, 0);
var maxDate = new Date(2013, 0, 1, 0, 0, 0, 0);
var delta = maxDate.getTime() - minDate.getTime();


var job_id = arg2;

var documentNumber = arg1;
var batchNumber = 5 * 1000;

var job_name = 'Job#' + job_id
var start = new Date();

var batchDocuments = new Array();
var index = 0;

var imageArray = [
  [
    {
      "origin": "/assets/imageDemo/origin1.jpg",
      "small": "/assets/imageDemo/small1.jpg",
      "thumb": "/assets/imageDemo/thumb1.jpg"
    }
  ],
  [
    {
      "origin": "/assets/imageDemo/origin2.jpg",
      "small": "/assets/imageDemo/small2.jpg",
      "thumb": "/assets/imageDemo/thumb2.jpg"
    }
  ],
  [
    {
      "origin": "/assets/imageDemo/origin3.jpg",
      "small": "/assets/imageDemo/small3.jpg",
      "thumb": "/assets/imageDemo/thumb3.jpg"
    }
  ],
  [
    {
      "origin": "/assets/imageDemo/origin4.jpg",
      "small": "/assets/imageDemo/small4.jpg",
      "thumb": "/assets/imageDemo/thumb4.jpg"
    }
  ],
  [
    {
      "origin": "/assets/imageDemo/origin5.jpg",
      "small": "/assets/imageDemo/small5.jpg",
      "thumb": "/assets/imageDemo/thumb5.jpg"
    }
  ],
  [
    {
      "origin": "/assets/imageDemo/origin6.jpg",
      "small": "/assets/imageDemo/small6.jpg",
      "thumb": "/assets/imageDemo/thumb6.jpg"
    }
  ],
  [
    {
      "origin": "/assets/imageDemo/origin7.jpg",
      "small": "/assets/imageDemo/small7.jpg",
      "thumb": "/assets/imageDemo/thumb7.jpg"
    }
  ],
  [
    {
      "origin": "/assets/imageDemo/origin8.jpg",
      "small": "/assets/imageDemo/small8.jpg",
      "thumb": "/assets/imageDemo/thumb8.jpg"
    }
  ],
  [
    {
      "origin": "/assets/imageDemo/origin9.jpg",
      "small": "/assets/imageDemo/small9.jpg",
      "thumb": "/assets/imageDemo/thumb9.jpg"
    }
  ],
  [
    {
      "origin": "/assets/imageDemo/origin10.jpg",
      "small": "/assets/imageDemo/small10.jpg",
      "thumb": "/assets/imageDemo/thumb10.jpg"
    }
  ],
  [
    {
      "origin": "/assets/imageDemo/origin11.jpg",
      "small": "/assets/imageDemo/small11.jpg",
      "thumb": "/assets/imageDemo/thumb11.jpg"
    }
  ],
  [
    {
      "origin": "/assets/imageDemo/origin12.jpg",
      "small": "/assets/imageDemo/small12.jpg",
      "thumb": "/assets/imageDemo/thumb12.jpg"
    }
  ],
  [
    {
      "origin": "/assets/imageDemo/origin13.jpg",
      "small": "/assets/imageDemo/small13.jpg",
      "thumb": "/assets/imageDemo/thumb13.jpg"
    }
  ],
  [
    {
      "origin": "/assets/imageDemo/origin14.jpg",
      "small": "/assets/imageDemo/small14.jpg",
      "thumb": "/assets/imageDemo/thumb14.jpg"
    }
  ],
  [
    {
      "origin": "/assets/imageDemo/origin15.jpg",
      "small": "/assets/imageDemo/small15.jpg",
      "thumb": "/assets/imageDemo/thumb15.jpg"
    }
  ],
];

var listBrand = ["Other", "Xeltek", "ATMEL", "ELNEC", "TOP", "SOFI-TECH", "Raspberry Pi", "ST",
  "InvenSense", "Eagle Power", "VISHAY", "Harvatek", "Andorin", "Sharp"];
var listOrigin = ["Việt Nam", "Trung Quốc", "Chính hãng", "Taiwan", "UK"];
var listLegType = ["NOPE", "DIP", "SIP", "LQFP", "PQFP", "QFN", "SOIC", "TQFP", "T-1 3/4", "SMD", "SMD0805"];
var listLegNumber = ["NOPE", "208", "100", "64", "44", "40", "32", "28", "24", "20", "16", "14", "8", "4"];
var listUnit = ["Cái", "Con", "Bộ"];
var listStock =  [1, 3, 7, 19, 15, 25, 60, 63, 70, 95, 150, 176, 210, 320, 333, 346, 425, 555, 100, 299];
var listName = ["Bộ Hẹn Giờ", "Cảm Biến Khoảng Cách ", "Module Điều Khiển ", "Case Arduino UNO", "Module Nguồn",
                "WiFi Serial Transceiver", "STM32F103VCT6 100-pin ARM ", "STM32F103ZET6 144-pin ARM ",
                "Bluetooth Module HC05 master / slave Breakout board", "Opamp Instrumentation Amplifier 1MHz",
                "10 - 80cm Distance Measuring ", "Full H-Bridge Driver Parallel 2A PowerSO-20"];
var listPrice = [
    [{num:1, price: 10000}],
    [{num:1, price: 15000}],
    [{num:1, price: 25000}],
    [{num:1, price: 35000}],
    [{num:1, price: 45000}],
    [{num:1, price: 55000}],
    [{num:1, price: 60000}],
    [{num:1, price: 100000}],
    [{num:1, price: 150000}],
    [{num:1, price: 300000}],
    [{num: 1, price: 10000},{num:10, price: 9000},{num:100,price:8000}],
    [{num: 1, price: 15000},{num:10, price: 14500},{num:100,price:14000}],
    [{num: 1, price: 30000},{num:10, price: 28000},{num:100,price:26000}],
    [{num: 1, price: 40000},{num:10, price: 35000},{num:100,price:30000}],
    [{num: 1, price: 90000},{num:10, price: 86000},{num:100,price:82000}],
    [{num: 1, price: 100000},{num:10, price: 90000},{num:100,price:80000}],
    [{num: 1, price: 150000},{num:10, price: 140000},{num:100,price:130000}],
    [{num: 1, price: 200000},{num:10, price: 190000},{num:100,price:180000}],
    [{num: 1, price: 300000},{num:10, price: 250000},{num:100,price:100000}]
];

var listSaleOff1 = [5, 10, 15];
var listSaleOff2 = [1000, 2000, 3000 , 4000];



while(index < documentNumber) {
  var group = listGroup[Math.floor(Math.random() * listGroup.length)];
  var stock = listStock[Math.floor(Math.random() * listStock.length)];
  var name = listName[Math.floor(Math.random() * listName.length)];
  var price = listPrice[Math.floor(Math.random() * listPrice.length)];

  var brand = listBrand[Math.floor(Math.random() * listBrand.length)];
  var origin = listOrigin[Math.floor(Math.random() * listOrigin.length)];
  var legType = listLegType[Math.floor(Math.random() * listLegType.length)];
  var legNumber = listLegNumber[Math.floor(Math.random() * listLegNumber.length)];
  var unit = listUnit[Math.floor(Math.random() * listUnit.length)];

  var groupUrl = getGroupUrl(group, listGroup);
  var subType = getSubType(group, listGroup);
  var subTypeUrl = getSubTypeUrl(group, listGroup);
  var supType = getSupType(group, listGroup);
  var supTypeUrl = getSupTypeUrl(group, listGroup);
  var createDate = new Date(minDate.getTime() + Math.random() * delta);
  var updateDate = new Date(minDate.getTime() + Math.random() * delta);
  var id = generateUUID();

  var listImage = imageArray[Math.floor(Math.random() * imageArray.length)];

  var saleOff1;
  var saleOff2;
  var tmp = Math.random()*100;
  if(tmp < 2){
    saleOff1 = listSaleOff1[Math.floor(Math.random() * listSaleOff1.length)];
    saleOff2 = 0;
  } else if(tmp < 4){
    saleOff1 = 0;
    saleOff2 = listSaleOff2[Math.floor(Math.random() * listSaleOff2.length)];
  } else {
    saleOff1 = 0;
    saleOff2 = 0;
  }


  var document = {
    _id : id,
    url: {
      supType: supTypeUrl,
      subType: subTypeUrl,
      group: groupUrl,
      pUrl: id
    },
    core: {
      code: id,
      name: name,
      price: price
    },
    info: {
      supType: supType,
      subType: subType,
      group: group,
      image: listImage,
      unit: unit,
      stock: stock,
      sold: Math.floor(Math.random() * 1000),
      vote: Math.floor(Math.random() * 100),
      brand: brand,
      origin: origin,
      legType: legType,
      legNumber: legNumber
    },

    extra: {
      saleOff1: saleOff1,
      saleOff2: saleOff2,
      info: "",
      note: ""
    }
  };

  batchDocuments[index % batchNumber] = document;
  if((index + 1) % batchNumber == 0) {
    db.product.insert(batchDocuments);
  }
  index++;
  if(index % 100000 == 0) {
    print(job_name + ' inserted ' + index + ' documents.');
  }
}
print(job_name + ' inserted ' + documentNumber + ' in ' + (new Date() - start)/1000.0 + 's');

