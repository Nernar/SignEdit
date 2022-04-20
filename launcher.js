ConfigureMultiplayer({
	isClientOnly: true
});

Callback.addCallback("API:KernelExtension", function() {
	Packages.io.nernar.innercore.signedit.SignEdit.setConfig(__config__);
}, 0);
