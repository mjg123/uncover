messagearea = document.getElementById("message")

function getPassword(){
    pws = []
    pw_inputs = document.getElementsByClassName("pw-input");

    for (i=0; i<pw_inputs.length; i++){
        pw = pw_inputs[i].value;
        if (pw != ""){
            pws.push(pw);}}

    pws = pws.sort();

    password = ""
    for (i=0; i<pws.length; i++){
        password += pws[i];}

    console.log("password is: " + password);

    return password
}

function decryptify(){
    triplesec.decrypt ({

        data:          new triplesec.Buffer(messagearea.value, "hex"),
        key:           new triplesec.Buffer(getPassword()),

    }, function (err, buff) {

        if (err) {
            alert(err);
        } else {
            messagearea.value = buff.toString();
        }

    });
}

document.getElementById("decrypt").onclick = decryptify

