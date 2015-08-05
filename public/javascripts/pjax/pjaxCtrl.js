
$(function() {
  // pjaxify links inside of #pjax-container

  //$(document).pjax('a.pjContainer', '#pjax-container');
  //$(document).pjax('a.pfull', '.main');
  //// pjaxify forms that with data-pjax attribute
  //$(document).on('submit', 'form[data-pjax]', function(event) {
  //  $.pjax.submit(event, '.main')
  //});

  // this is needed if you want the body of 400 etc. to show
  $(document).on('pjax:error', function(event, xhr, textStatus, errorThrown, options){
    if (xhr.status >= 400 && xhr.status < 500) {
      options.success(xhr.responseText, status, xhr);
    }
  });


  $(document).on('pjax:start', function() { NProgress.start(); });
  $(document).on('pjax:end',   function() {
    simpleCart.initialize();
    NProgress.done();
    if ($('#multizoom1').length)
    {
      $('#multizoom1').addimagezoom({ // multi-zoom: options same as for previous Featured Image Zoomer's addimagezoom unless noted as '- new'
        //descArea: '#description', // description selector (optional - but required if descriptions are used) - new
        speed: 0, // duration of fade in for new zoomable images (in milliseconds, optional) - new
        //descpos: true, // if set to true - description position follows image position at a set distance, defaults to false (optional) - new
        imagevertcenter: true, // zoomable image centers vertically in its container (optional) - new
        magvertcenter: true, // magnified area centers vertically in relation to the zoomable image (optional) - new
        zoomrange: [5, 10],
        magnifiersize: [450,450],
        magnifierpos: 'right',
        cursorshadecolor: '#fdffd5',
        cursorshade: true
      });
    }
    else {
      $(".zoomtracker").remove();
      $(".zoomstatus").remove();
      $(".cursorshade").remove();
      $(".magnifyarea").remove();
    }

  // Pjax-end!
  });


  $(document).on('click',".laside > div > span", function(){
    $(this).siblings("div").toggleClass("hide");
  });

  $(document).on('mouseenter', '.multizoom1 > a', function(){
    $(this).trigger('click');
    $('.tActive').toggleClass('tActive');
    $(this).children("img").first().toggleClass('tActive');
  });

  $(document).on('click', '.multizoom1 > a', function(){
    $('.tActive').toggleClass('tActive');
    $(this).children("img").first().toggleClass('tActive');
  });


  $(document).on("click", ".show-more a", function() {
    var $this = $(this);
    var $content = $this.parent().prev("ul");
    var linkText = $this.text();

    if(linkText === 'Ẩn bớt'){
      linkText = "Hiện thêm";
      $content.toggleClass("hideContent showContent");
    } else {
      linkText = "Ẩn bớt";
      $content.toggleClass("hideContent showContent");
    };
    $this.html(linkText);
  });

  $('.scroll-right').scrollbox({
    startDelay: 5,
    delay: 5,
    switchItems: 4,
    distance: 170*4.52
  });



  $('.s-prev').click(function () {
    $('.scroll-right').trigger('backward');
  });
  $('.s-next').click(function () {
    $('.scroll-right').trigger('forward');
  });


  var slider = $('#Glide').glide({
    afterTransition: function(){
      bLazy.revalidate();
    }
  });

  var slider_api = slider.data('glide_api');

  $(window).resize(function(){
    bLazy = new Blazy({
      success: function(element){
        var parent = element.parentNode.parentNode;
        parent.className = parent.className.replace(/\bloading\b/,'');
      }
    });
  });

});




var resizeElements;

$(document).ready(function() {

  // Set up common variables
  // --------------------------------------------------

  var bar = ".search_bar";
  var input = bar + " input[type='text']";
  var button = bar + " button[type='submit']";
  var dropdown = bar + " .search_dropdown";
  var dropdownLabel = dropdown + " > span";
  var dropdownList = dropdown + " ul";
  var dropdownListItems = dropdownList + " li";


  // Set up common functions
  // --------------------------------------------------

  resizeElements = function() {
    var barWidth = $(bar).outerWidth();

    var labelWidth = $(dropdownLabel).outerWidth();
    $(dropdown).width(labelWidth);

    var dropdownWidth = $(dropdown).outerWidth();
    var buttonWidth	= $(button).outerWidth();
    var inputWidth = barWidth - dropdownWidth - buttonWidth;
    var inputWidthPercent = inputWidth / barWidth * 100 + "%";

    $(input).css({ 'margin-left': dropdownWidth, 'width': inputWidthPercent });
  }

  function dropdownOn() {
    $(dropdownList).fadeIn(25);
    $(dropdown).addClass("active");
  }

  function dropdownOff() {
    $(dropdownList).fadeOut(25);
    $(dropdown).removeClass("active");
  }


  // Initialize initial resize of initial elements
  // --------------------------------------------------
  resizeElements();


  // Toggle new dropdown menu on click
  // --------------------------------------------------

  $(dropdown).click(function(event) {
    if ($(dropdown).hasClass("active")) {
      dropdownOff();
    } else {
      dropdownOn();
    }

    event.stopPropagation();
    return false;
  });

  $("html").click(dropdownOff);


  // Activate new dropdown option and show tray if applicable
  // --------------------------------------------------

  $(dropdownListItems).click(function() {
    $(this).siblings("li.selected").removeClass("selected");
    $(this).addClass("selected");

    // Focus the input
    $(this).parents("form.search_bar:first").find("input[type=text]").focus();

    var labelText = $(this).text();
    $(dropdownLabel).text(labelText);

    resizeElements();

  });


  // Resize all elements when the window resizes
  // --------------------------------------------------

  $(window).resize(function() {
    resizeElements();
  });
});