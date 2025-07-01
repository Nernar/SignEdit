#ifndef SIGNEDIT_BLOCKACTOR_H
#define SIGNEDIT_BLOCKACTOR_H

#include <stl.h>
#include <stl/string>

#ifndef SIGNEDIT_BLOCKSOURCE_H
	struct BlockPos;
#endif

enum BlockActorType : int {
	SIGN = 4
};

class BlockActor {
public:
	BlockPos& getPosition() const;
	bool isType(BlockActorType) const;
};

class SignBlockActor : public BlockActor {
public:
	stl::string& getMessage();
};

#endif
