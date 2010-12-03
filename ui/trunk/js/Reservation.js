var Reservation = function(params) {
  this.starttime = params.starttime;
  this.endtime = params.endtime;
  this.owner = params.owner;
  this.name = params.name;
};

Reservation.prototype.getParsedStartTime = function(){
  return this.starttime.substr(11,5).replace(':', '-');
};

Reservation.prototype.getParsedEndTime = function(){
  return this.endtime.substr(11,5).replace(':', '-');
};
