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
			$(".cartBar").animate({marginTop:0}, 100);
			$("#container").animate({marginTop:24}, 100);
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


  $(document).on('click', '.item_add', function(){
    var cart = $('.simpleCart_total');
    var imgtodrag = $(this).parent('.simpleCart_shelfItem').find("img").eq(0);

    if (imgtodrag) {
      var imgclone = imgtodrag.clone()
          .offset({
            top: imgtodrag.offset().top,
            left: imgtodrag.offset().left
          })
          .css({
            'opacity': '0.5',
            'position': 'absolute',
            'height': '150px',
            'width': '150px',
            'z-index': '1002'
          })
          .appendTo($('body'))
          .animate({
            'top': cart.offset().top + 10,
            'left': cart.offset().left + 10,
            'width': 75,
            'height': 75
          }, 400, 'easeInOutExpo');


      imgclone.animate({
        'width': 0,
        'height': 0
      }, function () {
        $(this).detach()
      });
    }
  });

}(jQuery,simpleCart))
