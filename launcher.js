ConfigureMultiplayer({
	isClientOnly: true
});
Launch();

Callback.addCallback("API:KernelExtension", function() {
	Packages.io.nernar.innercore.signedit.SignEdit.setConfig(__config__);
});
