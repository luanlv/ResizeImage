
$(function() {
  // pjaxify links inside of #pjax-container
  $(document).pjax('a', '#pjax-container');
  // pjaxify forms that with data-pjax attribute
  $(document).on('submit', 'form[data-pjax]', function(event) {
    $.pjax.submit(event, '#pjax-container')
  });

  // this is needed if you want the body of 400 etc. to show
  $(document).on('pjax:error', function(event, xhr, textStatus, errorThrown, options){
    if (xhr.status >= 400 && xhr.status < 500) {
      options.success(xhr.responseText, status, xhr);
    }
  });

  $(document).on('pjax:start', function() { NProgress.start(); });
  $(document).on('pjax:end',   function() { NProgress.done();  })
});