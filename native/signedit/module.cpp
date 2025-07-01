#include <logger.h>
#include <mod.h>
#include <nativejs.h>
#include <symbol.h>
#include <innercore/global_context.h>
#include <innercore/vtable.h>

#include <stl.h>
#include <stl/memory>
#include <stl/string>
#include <gsl/gsl-lite.hpp>

#include "includes/Actor.h"
#include "includes/BlockActor.h"
#include "includes/BlockSource.h"
#include "includes/ClientInstance.h"
#include "includes/StringHash.h"

class SignEditModule : public Module {
public:
	SignEditModule(): Module("signedit") {}

	virtual void initialize() {
		DLHandleManager::initializeHandle("libminecraftpe.so", "mcpe");
		// HookManager::addCallback(
		// 	SYMBOL("mcpe", "_ZN20SignScreenController17_registerBindingsEv"),
		// 	LAMBDA((SignScreenController* controller), {
		// 		StringHash<gsl::string_span> hash("#text_box_item_name");
		// 		controller->bindString(hash, stl::function<stl::string ()>() {
		// 			return SignEdit::openedSignMessage;
		// 		}, stl::function<bool ()>() {
		// 			return false;
		// 		});
		// 	}, ),
		// 	HookManager::CALL | HookManager::LISTENER
		// );
		HookManager::addCallback(
			SYMBOL("mcpe", "_ZN10StringHashC2IN3gsl17basic_string_spanIKcLin1EEEEERKT_"),
			LAMBDA((void* hash, const gsl::string_span& str), {
				Logger::info("Aboba", "size: %d", str.length());
			}, ),
			HookManager::CALL | HookManager::LISTENER
		);
	}
};

namespace SignEdit {
	stl::shared_ptr<UIScene> openedSignScreen;
	stl::string openedSignMessage;
	void pushSignScreen(LocalPlayer* player, SignBlockActor* sign) {
		ClientInstance* client = player->getClientInstance();
		if (client != nullptr) {
			VTABLE_FIND_OFFSET(ClientInstance_getCurrentSceneStack, _ZTV14ClientInstance, _ZNK14ClientInstance20getCurrentSceneStackEv);
			VTABLE_FIND_OFFSET(ClientInstance_getSceneFactory, _ZTV14ClientInstance, _ZNK14ClientInstance15getSceneFactoryEv);
			SceneStack* sceneStack = VTABLE_CALL<SceneStack*>(ClientInstance_getCurrentSceneStack, client);
			SceneFactory* sceneFactory = VTABLE_CALL<SceneFactory*>(ClientInstance_getSceneFactory, client);
			if (sceneStack != nullptr && sceneFactory != nullptr) {
				SignEdit::openedSignMessage = sign->getMessage();
				stl::shared_ptr<UIScene> scene = sceneFactory->createSignScreen(sign->getPosition());
				SignEdit::openedSignScreen = scene;
				sceneStack->pushScreen(scene, false);
			}
		}
	}
	void openSign(LocalPlayer* player, int x, int y, int z) {
		BlockSource* region = player->getRegion();
		if (region != nullptr) {
			BlockPos pos(x, y, z);
			BlockActor* blockActor = region->getBlockEntity(pos);
			if (blockActor != nullptr && blockActor->isType(BlockActorType::SIGN)) {
				SignEdit::pushSignScreen(player, (SignBlockActor*) blockActor);
			}
		}
	}
	void updateTextbox() {
		// AppPlatform* platform = GlobalContext::getAppPlatform();
		// platform->updateTextBoxText(SignEdit::openedSignMessage);
		// SignEdit::openedSignScreen->setTextboxText(SignEdit::openedSignMessage);
		Logger::info("SignEdit", SignEdit::openedSignMessage.c_str());
	}
};

extern "C" {
	void Java_io_nernar_signedit_SignEdit_openSign(JNIEnv* env, jint x, jint y, jint z) {
		SignEdit::openSign(GlobalContext::getLocalPlayer(), x, y, z);
	}
	void Java_io_nernar_signedit_SignEdit_updateTextbox(JNIEnv* env) {
		SignEdit::updateTextbox();
	}
}

MAIN {
	new SignEditModule();
}

JS_MODULE_VERSION(SignEdit, 1);

JS_EXPORT(SignEdit, openSign, "V(III)", (JNIEnv* env, jint x, jint y, jint z) {
	SignEdit::openSign(GlobalContext::getLocalPlayer(), x, y, z);
});
JS_EXPORT(SignEdit, updateTextbox, "V()", (JNIEnv* env) {
	SignEdit::updateTextbox();
});
