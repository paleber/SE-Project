let socket = undefined;

$(() => {

    socket = new WebSocket('ws://localhost:9000/socket');
    socket.onopen = () => {
        console.log("socket opened");
        socket.send("menu")
    };

    socket.onmessage = msg => {
        let data = JSON.parse(msg.data);
        console.log("socket received message: " + msg.data);
        handleMsg(data);
    };

    socket.onerror = msg => {
        console.log("socket error: " + msg);
        alert('An error occurred: ' + msg);
    };

    socket.onclose = () => {
        console.log("socket closed");
        $('.content').hide();
        $('#closed').show();
    };

    $('.levelBtn').click(() => {
        alert("Event");
        alert("Event" + $(this).val());
        socket.send("level " + $(this).val());
    });


});

showLevel = level => {
  if(socket !== undefined) {
      socket.send("game " + level);
  }
};

handleMsg = msg => {
    switch (msg.jsonClass) {
        case 'ServerMsg$ShowMenu$':
            $('.content').hide();
            $('#menu').show();
            break;
        case 'ServerMsg$ShowGame':
            $('.content').hide();
            $('#level').show();
            break;
        default:
            alert("Unhandled message: " + msg.jsonClass)
    }
};
