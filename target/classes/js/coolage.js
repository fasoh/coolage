
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

	$('#generateCoolage').prop('disabled', false);

	$('#generateCoolage').click(function() {

		var imagesString = '';

		$.each(images, function(id, image) {
			imagesString += image.url + ";";
		});

		$('#progress-hint').text('Bilder werden übertragen');

		var serverSocket = new WebSocket('ws://' + window.location.hostname + ':' + window.location.port + '/api/coolageSocket');

		serverSocket.onopen = function() {

			serverSocket.send(JSON.stringify({
				images: imagesString,
				text: $('#coolageText').val()
			}));
		};

		/*
		$.post('api/getCoolage', {
			images: imagesString,
			text: $('#coolageText').val()
		}, function(data, status) {
			
		});
		*/

		$('.progress-modal').modal('show');
		$('#coolage-progress').width('0%');
		$('#progress-hint').text('Bilder werden übertragen');


	});
});