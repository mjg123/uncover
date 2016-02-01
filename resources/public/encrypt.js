messagearea = document.getElementById("message")

is_encrypted = false;
passwd_cnt_input = document.getElementById("passwd_cnt");

function getPassword(){
    pws = []
    pw_inputs = document.getElementsByClassName("pw-input");

    for (i=0; i<pw_inputs.length; i++){
        pw = pw_inputs[i].value;
        if (pw != ""){
            pws.push(pw);}}

    pws = pws.sort();

    passwd_cnt_input.value = pws.length;

    password = ""
    for (i=0; i<pws.length; i++){
        password += pws[i];}

    console.log("password is: " + password);

    return password
}

function encryptify(){
    if (getPassword() === ""){
        alert("No point doing that without a password!");
        return;
    }
    if (is_encrypted){
        alert("Already encrypted");
        return;
    }
    triplesec.encrypt({
        data:  new triplesec.Buffer(messagearea.value),
        key:   new triplesec.Buffer(getPassword())
    }, function (err, buf) {
        if (!err) {
            var ciphertext = buf.toString('hex');
            messagearea.value = ciphertext;
            is_encrypted = true;
        }
    })
}

document.getElementById("encrypt").onclick = encryptify


function oneMorePassword(){
    var newPwInput = document.createElement("input");
    newPwInput.className = "form-control pw-input";
    newPwInput.autocomplete = "off";
    newPwInput.placeholder = "Another one";

    var lastPw = document.getElementById("dummy-last-password");
    lastPw.parentNode.insertBefore(newPwInput, lastPw);
}

document.getElementById("onemorepw").onclick = oneMorePassword
