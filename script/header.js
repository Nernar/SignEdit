/*

   Copyright 2022-2024 Nernar (github.com/nernar)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

const SignEdit = {
	VANILLA_SIGN_IDS: [
		"oak_sign", "birch_sign", "spruce_sign", "dark_oak_sign",
		"acacia_sign", "jungle_sign", "warped_sign", "crimson_sign"
	],
	TILE_ENTITY_TYPE: ETileEntityType.SIGN || 4,
	isSignRequiredToEdit() {
		return this.signRequiredToEdit || false;
	},
	setIsSignRequiredToEdit(required) {
		this.signRequiredToEdit = !!required;
	},
	registerSign(id) {
		if (typeof id != "number") {
			id = VanillaItemID[id] || ItemID[id];
		}
		if (id == null || id == 0) {
			Logger.error("ERROR", "SignEdit: Sign id " + arguments[0] + " not even registered!");
			return false;
		}
		this.registeredSigns || (this.registeredSigns = []);
		if (this.registeredSigns.indexOf(id) != -1) {
			Logger.debug("INFO", "SignEdit: Sign id " + IDRegistry.getStringIdForIntegerId(id) + " is already registered!");
			return false;
		}
		this.registeredSigns.push(id);
		return true;
	},
	isRegisteredSign(id) {
		return this.registeredSigns != null && this.registeredSigns.indexOf(id) != -1;
	}
};

for (let index = 0; index < SignEdit.VANILLA_SIGN_IDS.length; index++) {
	SignEdit.registerSign(SignEdit.VANILLA_SIGN_IDS[index]);
}

SignEdit.setIsSignRequiredToEdit(
	__config__.getBool("sign_required_to_edit")
);

ModAPI.registerAPI("SignEdit", SignEdit);
