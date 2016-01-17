
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

		serverSocket.onmessage = function(message) {
			message = JSON.parse(message.data);

			if (message.percentage) {
				$('#coolage-progress').width(message.percentage + '%');
			}
			if (message.task) {
				$('#progress-hint').text(message.task);
			}
		};

		$('.progress-modal').modal('show');
	});
});