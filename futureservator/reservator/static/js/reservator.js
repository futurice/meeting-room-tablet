function reservator_init(monday, nextmonday) {
	var date_re = '';
	var time_re = new RegExp('(\\d\\d):(\\d\\d)');

	var container_div = $('#reservations');

	// set style for container_div
	container_div.addClass('week-calendar');
	var w = container_div.width();
	var h = container_div.height();

	var reservation_divs = $('.reservation');

	reservation_divs.each(function(i, r){
		r = $(r);
		var cancelable = r.hasClass('cancelable');

		var start_span = jQuery('.start', r);
		var end_span   = jQuery('.end', r);
		var date_span  = jQuery('.date', r);
		var dow_span   = jQuery('.dayofweek', r);
		var debug_span = jQuery('.debug', r);
		var form       = jQuery('form', r);
		var subject_span = jQuery('.subject', r);

		var dow = (parseInt(dow_span.text()) + 6) % 7; // Sunday  == 0 -> Monday = 0
		console.log(dow);

		start_m = time_re.exec(start_span.text());
		end_m = time_re.exec(end_span.text());

		if (parseInt(end_m[1]) == 0 && parseInt(end_m[2]) == 0) end_m[1] = 24;

		start_x = (start_m[1])*h/24;
		end_x = (end_m[1])*h/24;

		// console.log(start_m, start_x, end_m, end_x);

		// dimensions
		r.css('width', w/7);
		r.css('height', end_x - start_x-2);

		r.css('left', w*dow/7);
		r.css('top', start_x);

		// hidding nonneeded spans
		debug_span.css('display', 'none');
		dow_span.css('display', 'none');

		var buttons = {};

		// extra for the cancelable
		if (cancelable) {
			// hidding form
			form.css('display', 'none');
			buttons = { "Delete":  function() { $(this).dialog("close"); form.submit(); } }
		}


		r.click(function() {
			$('<div></div>').html(
				date_span.text() + " " + start_span.text() + "-" + end_span.text() + "<br />" + subject_span.text()
			).dialog({
				title: 'Reservation',
				modal: true,
				buttons: buttons
			})
		});
	});
}
