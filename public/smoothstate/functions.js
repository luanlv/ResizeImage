;(function ($) {
    'use strict';
    var $body = $('html, body'),
        $main = $('#main'),
        options = {
            prefetch: false,
            pageCacheSize: 4,
            onStart: {
                duration: 0,
                render: function (url, $container) {
                    //$body.animate({
                    //    scrollTop: 0
                    //});
                    $main.addClass('is-exiting');
                    smoothState.restartCSSAnimations();
                    NProgress.start();
                }
            },
            onEnd: {
                duration: 0,
                render: function (url, $container, $content) {
                    $main.removeClass('is-exiting');
                    $main.html($content);
                    $body.css('cursor', 'auto');
                    $body.find('a').css('cursor', 'auto');
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
                }
            }
        },
        smoothState = $main.smoothState(options).data('smoothState');
})(jQuery);
