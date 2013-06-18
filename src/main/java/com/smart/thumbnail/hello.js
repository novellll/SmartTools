var page = require('webpage').create();
phantom.addCookie({
    "name": "smart.sid",
    "value": "s%3AR%2BPvKM22ie4H5vRprW2es%2BOZ.iL46wZD2LOtaMemu0qZoTELQ7%2By3%2FYahBJrp0mkkWWc",
    "domain": "127.0.0.1"
});
page.open('http://127.0.0.1:3000/tmpl?tmplid=5075354ccf8a77ee3d00000d', function () {
    page.render('message.png');
    phantom.exit();
});
