import os
import json

def gen_age_mod(age, rooted, berry_name):
	model_dict = {
		0: "cobblemon:block/berries/planted_bound" if rooted else "cobblemon:block/berries/planted",
		1: "cobblemon:block/berries/sprout",
		2: f"cobblemon:block/berries/{berry_name}_young",
		3: f"cobblemon:block/berries/{berry_name}_mature",
		4: f"cobblemon:block/berries/{berry_name}_mature",
		5: f"cobblemon:block/berries/{berry_name}_mature"
	}
	result = dict()
	when_dict = dict()
	and_list = list()
	and_list.append({
		"age": age
	})
	and_list.append({
		"rooted": rooted
	})
	when_dict["AND"] = and_list
	apply_dict = {
		"model": model_dict[age],
		"uvlock": False
	}
	result["when"] = when_dict
	result["apply"] = apply_dict
	return result

def gen_mulch_mod(mulch):
	result = dict()
	when_dict = {
		"mulch": mulch
	}

	apply_dict = {
		"model": f"cobblemon:block/{mulch}_mulch"
	}
	result["when"] = when_dict
	result["apply"] = apply_dict
	return result



if __name__ == "__main__":
	berry_files = os.listdir("../common/src/main/resources/data/cobblemon/berries")
	for f in berry_files:
		berry_name = f[:-11]
		file = open(f"blockstate/{f}", "w+")
		multipart_list = list()
		for i in range(0, 6):
			multipart_list.append(gen_age_mod(i, False, berry_name))
			multipart_list.append(gen_age_mod(i, True, berry_name))
		mulch_list = (
			"coarse",
			"growth",
			"humid",
			"loamy",
			"peat",
			"rich",
			"sandy",
			"surprise"
		)
		for mulch in mulch_list:
			multipart_list.append(gen_mulch_mod(mulch))
		file.write(json.dumps({
			"multipart": multipart_list
			}, indent=2))
		file.close()