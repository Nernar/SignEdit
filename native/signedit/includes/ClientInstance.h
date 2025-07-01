#ifndef SIGNEDIT_CLIENTINSTANCE_H
#define SIGNEDIT_CLIENTINSTANCE_H

#include <stl.h>
#include <stl/memory>
#include <stl/string>

#ifndef SIGNEDIT_BLOCKSOURCE_H
	struct BlockPos;
#endif
#ifndef SIGNEDIT_STRINGHASH_H
	class StringHash;
#endif

class ClientInstance {};

class AppPlatform {
public:
	void updateTextBoxText(const stl::string&);
};

class AbstractScene {};

class UIScene : public AbstractScene {
public:
	void setTextboxText(const stl::string&);
};

class SceneFactory {
public:
	stl::shared_ptr<UIScene> createSignScreen(const BlockPos&);
};

class SceneStack {
public:
	void pushScreen(stl::shared_ptr<AbstractScene>, bool);
};

class ScreenController {
public:
	void bindString(const StringHash&, const stl::function<stl::string ()>&, const stl::function<bool ()>&);
};

class SignScreenController : public ScreenController {};

#endif
