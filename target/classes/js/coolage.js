
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

		$('#final-image').hide();
		$('#server-progress').show();
		$('#cancel').show();
		$('#done').hide();

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
			if (message.image) {
				$('#final-image').attr('src', 'http://' + window.location.hostname + ':' + window.location.port + message.image);
				$('#server-progress').hide();
				$('#final-image').show();
				$('#cancel').hide();
				$('#done').show();
			}
		};

		$('.progress-modal').modal('show');
	});
});