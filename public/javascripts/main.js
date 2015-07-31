(function(jQuery, simpleCart){
	// simpleCart setup
	simpleCart.email = "luanlv2591@gmail.com";
	simpleCart.checkoutTo = PayPal;
	simpleCart.currency = "VND";
	//simpleCart.successUrl = "http://basicblackframe.com/thank-you.html";
	//simpleCart.cancelUrl = "http://basicblackframe.com/order-black-frames.html";
	simpleCart.cartHeaders = ["Name" ,  "Price", "Quantity", 'Total', 'remove'];
	simpleCart.sandbox = false;
	simpleCart.paypalHTTPMethod = "GET";


	var lowerCartBar = function(){
			$(".cartBar").animate({marginTop:0}, 100);
			$("#container").animate({marginTop:24}, 100);
      if($(".zoomtracker").length) {
        $(".zoomtracker").animate({marginTop: 24}, 100);
      }
	};

	$(function(){

		var running_total = $("#running-total"),
			quantity = $("#item-quantity"),
			price_input = $(".item_price"),


			// update the size input

			// update the price input

			// update the 'running total' on the page
		 	updateTotal = function(){
				var itemObject = {
					quantity: quantity.val(),
					price: price_input.val()
				};

				// check the price and quantity
				//CartItem.prototype.checkQuantityAndPrice.call( itemObject );
				// calculate the cost based on the current values
				running_total.html( simpleCart.valueToCurrencyString( itemObject.quantity * itemObject.price ) );
			},

			// calculate the price of a frame


			// reset form
			reset = window['reset'] = function(){


				// reset quantity
				quantity.val(1);

				updateTotal();
			};

		// view cart
		$("#view_cart").click(function(){
		    if( $(this).hasClass('open') ){
				$(this).text('Chi tiết');
				$("#cart").slideUp();
		    } else {
				$(this).html('Ẩn đi');
				$("#cart").slideDown();
		    }
		    $(this).toggleClass('open');
		});

    $(".simpleCart_empty").click(function(){
      $(".cartBar").animate({marginTop:-24}, 200);
      $("#container").animate({marginTop:0}, 200);
      if($(".zoomtracker").length){
        $(".zoomtracker").animate({marginTop:0}, 100);
      }
      var x = $("#view_cart");
      if( x.hasClass('open') ){
        if( x.hasClass('open') ){
          x.text('Chi tiết');
          $("#cart").slideUp();
        } else {
          $(this).text('Ẩn đi');
          $("#cart").slideDown();
        }
        x.toggleClass('open');
        $("#cart").attr("style", "");
      }
    });

		// handle quantity
		$(".item_quantity").change(function(){
			updateTotal();
		});

		reset();
	});


	simpleCart.ready(function(){

		simpleCart.bind('afterAdd',function(item,isNew){
      if(simpleCart.quantity){
        lowerCartBar();
      }
		});

    simpleCart.bind('afterRemove',function(item,isNew){
      if(!simpleCart.quantity){
        $(".simpleCart_empty").trigger('click');
      }
    });


		if(simpleCart.quantity){
			lowerCartBar();
		}

    $('.item_add').prop("disabled", false); // Element(s) are now enabled.

	});


}(jQuery,simpleCart));
