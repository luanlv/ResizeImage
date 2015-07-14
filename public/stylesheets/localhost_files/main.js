(function(jQuery, simpleCart){
	// simpleCart setup
	simpleCart.email = "info@mframing.com";
	simpleCart.checkoutTo = PayPal;
	simpleCart.currency = USD;
	simpleCart.successUrl = "http://basicblackframe.com/thank-you.html";
	simpleCart.cancelUrl = "http://basicblackframe.com/order-black-frames.html";
	simpleCart.cartHeaders = ["Name" , "Size", "Price", "Options", "Quantity" ];
	simpleCart.sandbox = false;
	simpleCart.paypalHTTPMethod = "GET";

	simpleCart.shipping = function(){
		// return 0 if there is nothing in the cart or if the order is $100 or more
		return simpleCart.quantity === 0 || simpleCart.total >= 100 ? 0 : 20;
	};
	
	CartItem.prototype.sku = function(){
		var base_sku =  this.name == "1 1/2\" Large Black Cap" ? "102" :
						this.name == "1 1/4\" Large Black Cap" ? "101" :
						this.name == "2 1/4\" Large Black Cap" ? "103" :
						"104";
		if( this.options == "None" ){
			return base_sku + "-0";
		} else {
			return base_sku + "-1";
		}
	};

	CartItem.prototype.gaVariation = function(){
		return this.size;
	};
	
	
	var lowerCartBar = function(){
			$(".cartBar").animate({marginTop:0}, 200);
			$("#container").animate({marginTop:24}, 200);
	};
	
	$(function(){
		
		var name_input = $(".item_name").val( $(this).children(".frame-name").text() ),
			running_total = $("#running-total"),
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
				CartItem.prototype.checkQuantityAndPrice.call( itemObject );

				// calculate the cost based on the current values
				running_total.html( simpleCart.valueToCurrencyString( itemObject.quantity * itemObject.price ) );
			},

			// calculate the price of a frame


			// reset form
			reset = window['reset'] = function(){

				// reset type
				$(".choose-frame-btn").removeClass('selected');
				// reset options
				$.uniform.update();

				// reset quantity
				quantity.val(1);

				updateTotal();
			};
		    


			
		// view cart
		$("#view_cart").click(function(){
		    if( $(this).hasClass('open') ){
				$(this).text('Xem');
				$("#cart").slideUp();
		    } else {
				$(this).text('Ẩn ');
				$("#cart").slideDown();
		    }

		    $(this).toggleClass('open');
		});

    $(".simpleCart_empty").click(function(){
      $(".cartBar").animate({marginTop:-24}, 200);
      $("#container").animate({marginTop:0}, 200);
    });
		
		// handle quantity 
		$(".item_quantity").change(function(){
			updateTotal();
		});
		
		// handle style

		
		// buy more button
		
		/*****************************
		Uniform Custom Form Elements
		*****************************/
		if( $('body').hasClass('order-page') ){
			$('input:checkbox, input:radio').uniform();
		}
		
		$(".cartHeaders .itemframestyle").text("Frame Style");
		
		reset();
	});	
	
	
	simpleCart.ready(function(){

		simpleCart.bind('afterAdd',function(item,isNew){
      if(simpleCart.quantity){
        lowerCartBar();
      }
      $("#showCartAdded").trigger("click");
		});

		$("#showCartAdded").fancybox({
      autoDimensions : false,
      'hideOnContentClick': true,
			autoScale: true,
			scrolling: "no",
			onClosed: function(){
				reset();
			}
		});

		$("#buy-more-frames").click(function(){
			$.fancybox.close();
		});
		
		if(simpleCart.quantity){
			lowerCartBar();
		}
		
	});
	


}(jQuery,simpleCart))
