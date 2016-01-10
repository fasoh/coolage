
var startCoolage;
var images = [];

$(function() {
	startCoolage = function(files) {
		images = files;
		if (images.length > 0 && $('#coolageText').val() != "") {
			$('#generateCoolage').prop('disabled', false);
		} else {
			$('#generateCoolage').prop('disabled', true);
		}
	};

	$('#coolageText').keyup( function() {
		if (images.length > 0 && $('#coolageText').val() != "") {
			$('#generateCoolage').prop('disabled', false);
		} else {
			$('#generateCoolage').prop('disabled', true);
		}
	});

	$('#generateCoolage').click(function() {
		$.post('api/getCoolage', {
			images: images,
			text: text
		}, function(data, status) {
			alert(data);
		});
	});
});