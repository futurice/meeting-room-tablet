var FR = function(){
    var rooms = [
		{floor : 4, name : 'Panorama', number : 401, location : [978,354,118,82] },
		{floor : 4, name : 'Pilotti', number : 402, location : [1135,354,118,82] },
		{floor : 4, name : 'Vauhtimato', number : 403, location : [1220,354,118,82] },
		{floor : 4, name : 'Space Shot', number : 404, location : [979,184,178,129] },
		{floor : 4, name : 'Regatta', number : 405, location : [10,354,118,85] },
		{floor : 4, name : 'Kino', number : 406, location : [1135,56,118,85] },
		{floor : 4, name : 'Vekkula', number : 407, location : [978,56,118,85] },
		{floor : 4, name : 'Metkula', number : 408, location : [775,56,197,85] },
		{floor : 4, name : 'Regatta', number : 405, location : [1220,56,118,85] },
		{floor : 4, name : 'Keittiö', number : 499, location : [10,186,142,130,10,316,480,120] },
	];
	
	var reservations = [
		{name : 'Kino', owner : 'Mats', starttime : '2010-12-03 13:00:00.000', endtime : '2010-12-03 15:50:00.000'},
		{name : 'Keittiö', owner : 'Mats', starttime : '2010-12-03 11:00:00.000', endtime : '2010-12-03 16:00:00.000'},
		{name : 'Metkula', owner : 'Mats', starttime : '2010-12-03 08:00:00.000', endtime : '2010-12-03 10:00:00.000'}
	];

	var populateRooms = function() {
		$('<div><div id="room_${number}" style="left:${location[0]}"><b>${name}</b> (${number})</div></div>').tmpl( rooms )
        .appendTo( "#floormap_4" );
	};
	
    populateRooms();
	return {
    };
}();
