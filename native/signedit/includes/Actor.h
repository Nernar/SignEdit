#ifndef SIGNEDIT_ACTOR_H
#define SIGNEDIT_ACTOR_H

#ifndef SIGNEDIT_BLOCKSOURCE_H
	class BlockSource;
#endif
#ifndef SIGNEDIT_CLIENTINSTANCE_H
	class ClientInstance;
#endif

class Actor {
public:
	BlockSource* getRegion() const;
};

class LocalPlayer : public Actor {
public:
	ClientInstance* getClientInstance() const;
};

#endif
