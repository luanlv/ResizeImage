/**
 * Created by lvl on 7/12/15.
 */

var component = function(json){
    return '<li class="ra-5">' +
    '<div class="img">' +
    '<img src="//cdn.lvlblog.com/image/v1/thumb/'+ json.image + '" alt="">' +
    '</div>' +
    '<div class="codeP">' +
    '<a class="pjContainer" href="/product/view/' +json.code + '"><b>' + json.code + '</b></a>' +
    '<div class="stock"> (' + json.stock + ')</div>' +
    '</div>' +
    '<div class="nameP">'+ json.name +'</div>' +
    '<div class="priceP">Giá bán lẻ:' +
    '<div class="price"><b>' + json.price + ' VNĐ</b></div>' +
    '</div>' +
    '</li>';
};
var template = function(jsonArray){
  var result = '<ul class="gallery">';
  var j;
  for (j = 0; j< jsonArray.length; j++){
    result += component(jsonArray[j]);
  }
  result += '</ul>';
  return result;
};

BigPipe.renderPagelet = function(id, json) {
  var domElement = document.getElementById(id);
  if (domElement) {
    var fullTemplate = template(json);
    domElement.innerHTML
        = fullTemplate;
  } else {
    console.log("ERROR: cannot render pagelet because DOM node with id " + id + " does not exist");
  }
};