
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

		var imagesString = '';

		$.each(images, function(id, image) {
			imagesString += image.url + ";";
		});

		$.post('api/getCoolage', {
			images: imagesString,
			text: $('#coolageText').val()
		}, function(data, status) {
			
		});
	});
});