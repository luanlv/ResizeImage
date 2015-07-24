
$(function() {
  // pjaxify links inside of #pjax-container
  $(document).pjax('a.pjContainer', '#pjax-container');
  $(document).pjax('a.pfull', '.main');
  // pjaxify forms that with data-pjax attribute
  $(document).on('submit', 'form[data-pjax]', function(event) {
    $.pjax.submit(event, '.main')
  });

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
        cursorshade: true //<-- No comma after last option!
      });

    }
    else {
      $(".zoomtracker").remove();
      $(".zoomstatus").remove();
      $(".cursorshade").remove();
      $(".magnifyarea").remove();
    }

    if ($('#lightbox').length) {
      $('#lightbox a').slimbox();
      console.log("slimbox initial");
    }



  // Pjax-end!
  });



  $(document).on('click',".laside > div > span", function(){
    $(this).siblings("div").toggleClass("hide");
  });

  $(document).on('mouseenter', '.multizoom1 > a', function(){
    $(this).trigger('click');
  });


});