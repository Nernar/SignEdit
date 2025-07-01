#ifndef SIGNEDIT_STRINGHASH_H
#define SIGNEDIT_STRINGHASH_H

#include <gsl/gsl-lite.hpp>

class StringHash {
public:
	StringHash(const gsl::string_span&);
};

#endif
