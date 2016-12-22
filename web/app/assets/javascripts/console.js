let socket = undefined;

$(() => {

    socket = new WebSocket('ws://localhost:9000/socket');
    socket.onopen = () => {
        console.log("socket opened");
        socket.send("menu")
    };

    socket.onmessage = msg => {
        let data = JSON.parse(msg.data);
        let type = data.jsonClass;
        delete data.jsonClass;
        let value = JSON.stringify(data).slice(1, -1);
        if(value.length > 0) {
            value = "<br>" + value;
        }
        console.log("socket received message: " + msg.data);
        $('#messages').prepend(
            "<div class='alert alert-success'>" +
            "<strong>" + type + " </strong>" + value +
            "</div>"
        );
    };

    socket.onerror = msg => {
        console.log("socket error: " + msg);
        alert('An error occurred: ' + msg);
    };

    socket.onclose = () => {
        console.log("socket closed");
        $('#cmdWell').hide();
        $('#closed').show();
    };

    $('#inputCmd').keypress( event => {
        const keycode = (event.keyCode ? event.keyCode : event.which);
        if(keycode == '13'){
            sendCmd();
        }
    });

});

sendCmd = () => {
    if (socket !== undefined) {
        let inputField = $('#inputCmd');
        socket.send(inputField.val());
        inputField.val("")
    }
};