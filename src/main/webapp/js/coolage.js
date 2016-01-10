
var startCoolage;

$(function() {
	startCoolage = function(files) {
		out='';
		for(var i=0;i < files.length;i++) {
			out+=event.fpfiles[i].url;
			out+=' '
		};
		alert(out);
	}
});