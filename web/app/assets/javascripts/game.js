$(() => {
    $.ajax({
        url: '/loadState',
        success: msgBuffer => {
            JSON.parse(msgBuffer).forEach(msg => handleMsg(msg))
        },
        error: () => {
            alert('An error occurred')
        }
    });

    let socket = new WebSocket('ws://localhost:9000/socket');
    socket.onopen = () => alert('onopen');
    socket.onmessage = msg => handleMsg(msg);
    socket.onerror = () => alert('onerror');
    socket.onclose = () => alert('onclose');

});

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
