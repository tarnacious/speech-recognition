(function() {
    var polyFill = {
        getUserMedia: function(c) {
        return new Promise(function(y, n) {
            (navigator.mozGetUserMedia ||
            navigator.webkitGetUserMedia).call(navigator, c, y, n);});
        }
    }
    navigator.mediaDevices = navigator.mediaDevices || ((navigator.mozGetUserMedia || navigator.webkitGetUserMedia) ? polyFill : null);
})()


function App() {
    this.el = document.querySelectorAll("#output")[0]
    this.record = document.querySelectorAll("#record")[0]
    this.stream;
    this.xhr;
    this.processor;
    this.data = [];
    this.counter = 0;
    this.recording = false;
    this.context;
    this.audioInput;

    if (!navigator.mediaDevices) {
        console.log("getUserMedia() not supported.");
        return;
    }
    this.bindEvents()
    this.start_recording()
}

App.prototype.bindEvents = function() {
    this.record.onclick = function() {
        this.start_listening()
    }.bind(this);
}

App.prototype.onResponse = function() {
    if (this.xhr.readyState != 4) return; 
    var response = JSON.parse(this.xhr.responseText);
    this.el.innerHTML = "Understood: " + response.text 
    var audio1 = new Audio("/data/" +  response.ok + ".wav");
    audio1.play();
}
   
App.prototype.postData = function() {
    this.processor.disconnect()
    this.el.innerHTML = "analysing" 
    this.xhr = new XMLHttpRequest();
    this.xhr.open("POST", "/", true);
    this.xhr.setRequestHeader('Accept', 'application/json');
    this.xhr.setRequestHeader('Content-Type', 'application/octet-stream');
 
    var size = this.data.reduce(
        function(acc, next) { return acc + next.length }, 0)
    this.all = new Float32Array(size);
    var position = 0;
    var i;
    for(i = 0; i < this.data.length; i++) {
        this.all.set(this.data[i], position)
        position = position + this.data[i].length
    }

    this.xhr.send(this.all);
    this.xhr.onreadystatechange = this.onResponse.bind(this);  
}

App.prototype.recorderProcess = function(e) {
    var left = e.inputBuffer.getChannelData(0);
    this.counter = this.counter + 1;
    //var arr = Array.prototype.slice.call(left)
    this.data.push(new Float32Array(left))
    if (this.counter != 100) return; 
    this.postData()
} 

App.prototype.start_listening = function() {
    this.el.innerHTML = "Listening.."
    this.data = []
    this.counter = 1
    this.audioInput = this.context.createMediaStreamSource(stream);
    this.processor = this.context.createScriptProcessor() 
    this.processor.onaudioprocess = this.recorderProcess.bind(this)
    this.processor.connect(this.context.destination)
    this.audioInput.connect(this.processor);
}

App.prototype.gotStream = function(stream) {
    this.stream = stream;
    document.getElementById("permissions").style.display = 'none';
    document.getElementById("application").style.display = 'block';
}

App.prototype.streamError = function(err) {
    console.log("error!");
    console.log(err.name + ": " + err.message);
}

App.prototype.start_recording = function() {
    var constraints = { audio: true };
    userMedia = navigator.mediaDevices.getUserMedia(constraints);
    userMedia.then(this.gotStream).catch(this.streamError);
    this.context = new AudioContext();
}

var app;
window.onload = function() { app = new App() };
