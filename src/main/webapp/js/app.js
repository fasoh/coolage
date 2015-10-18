// Das wir hier "/socket" verwenden, wird in ChatServlet.java festgelegt.
var connection = new WebSocket(window.location.href.replace(/http/, 'ws').replace(/\/$/, '') + '/socket'),
    $timeline = $('#timeline'),
    $message = $('#message');

/**
 * Wird aufgerufen, wenn der Versuch eine Verbindung zum Server herzustellen (s.o.: "... new WebSocket...") erfolgreich
 * war.
 */
connection.onopen = function () {
    $timeline.append('<li class="system-message">[Verbindung hergestellt. Öffne einen zweiten Browser um mit dir selbst zu reden.]</li>')
};

/**
 * Tritt ein Fehler beim Verbinden mit dem Server oder danach auf, wird diese Funktion aufgerufen.
 */
connection.onerror = function () {
    $timeline.append('<li class="system-message">[Es ist ein Fehler aufgetreten. Lade die Seite, falls das nicht hilft starte den Server neu und probiere es erneut.]</li>')
};

/**
 * Wird ausgeführt wenn der Browser eine Nachricht vom Server bekommt. Die gesendeten Daten (e.data) werden als
 * Listenelement in die Liste in index.html eingetragen.
 * @param e
 */
connection.onmessage = function (e) {
    $timeline.append('<li>' + e.data + '</li>');
};

/**
 * Hier wird alle 2 Sekunden ein "Ping" (irgendeine Nachricht ohne besonderen Inhalt) an den Server geschickt,
 * um ihm zu zeigen das der Browser noch offen ist. Ansonsten würde der Server die Verbindung nach einem bestimmten
 * Timeout (siehe ChatServlet.java) trennen.
 */
setInterval(function() {
    connection.send('---ping');
}, 2000);

/**
 * Bei einem Klick auf den Senden Button (id="send" in index.html) wird die Nachricht aus dem Testfeld an den Server
 * gesendet und anschließend das Textfeld geleert.
 */
$('#send').on('click', function(e) {
    connection.send($message.val());

    $message.val('');
});

/**
 * Drückt man im Textfeld die Enter-Taste wird ein Klick auf den Senden Button simuliert (e.which enhält immer den
 * Code der Taste die gerdrückt wurde, 13 = Enter).
 */
$message.on('keypress', function(e) {
   if (e.which == 13) $('#send').click();
});