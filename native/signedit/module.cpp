#include <logger.h>
#include <mod.h>
#include <symbol.h>

class SignEditModule : public Module {
public:
	SignEditModule(): Module("signedit") {}

	virtual void initialize() {
		DLHandleManager::initializeHandle("libminecraftpe.so", "mcpe");
	}
}

MAIN {
	new SignEditModule();
}
