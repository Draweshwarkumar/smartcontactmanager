console.log("This is Script file");

const toggleSidebar = () => {
	if($(".sidebar").is(":visible")){
		
		$(".sidebar").css("display","none");
		$(".content").css("margin-left","0%");
	}
	else{
		$(".sidebar").css("display","block");
		$(".content").css("margin-left","20%");
	}
};

const search = () =>{
	let query = $("#search-input").val();

	if(query == ""){
		$(".search-result").hide();
	}
	else{
		console.log(query);


		 let url = `http://localhost:8181/search/${query}`;

		 fetch(url)
		 .then((response) => {
			return response.json();
		 })
		 .then((data) =>{
			console.log(data);

			let text = `<div class='list-group'>`

			data.forEach((contact) =>{
				text += `<a href='/user/${contact.cId}/contact' class= 'list-group-item list-group-action'> ${contact.name}  </a>`
			});

			text+=`</div>`;

			$(".search-result").html(text);
			$(".search-result").show();

		 });

	}
};

//first request to server to create order

const paymentStart = () => {
	console.log("payment started..");
	var amount = $("#payment_field").val();
	console.log(amount);
	if(amount == "" || amount == null){
		alert("amount is required !!");
		return;
	}
	
	//we will use ajax to send request to server to create order- jquery

	$.ajax(
		{
			url : '/user/create_order',
			data:JSON.stringify({amount:amount,info:'order_request'}),
			contentType: 'application/json',
			type:'POST',
			dataType:'json',
			success:function(response){
				//invoked when success
				console.log(response);
				if(response.status == "created"){
					let options = {
						key:'',
						amount:response.amount,
						currency:'INR',
						name: 'Smart Contact Manager',
						image:'',
						order_id:response.id,
						handler:function(response){
							console.log(response.razorpay_payment_id)
							console.log(response.razorpay_order_id)
							console.log(response.razorpay_signature)
							console.log('payment successful !!')
							//alert("congrates !! Payment successful !!")
							Swal.fire({
							  title: "Good job!",
							  text: "congrates !! Payment successful !!",
							  icon: "success"
							});
						},
						prefill:{
							name: "",
							email: "",
							contact: "",
						},
						
						notes: {
							address: "Shivam jha"
						},
						theme:{
							color: "#3399cc",
						},
					};
					
					let rzp = new Razorpay(options);
					rzp.on("payment.failed", function(response){
						console.log(response.error.code);
						console.log(response.error.description);
						console.log(response.error.source);
						console.log(response.error.step);
						console.log(response.error.reason);
						console.log(response.error.metadata.order_id);
						console.log(response.error.metadata.payment_id);
						alert("Oops payment failed !!");
					});
					
					rzp.open();
				}
			},
			error:function(error){
				//invoked when error
				console.log(error)
				alert("something went wrong !!")
			}
		}
	)
};