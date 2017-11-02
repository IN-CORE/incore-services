const devConfig = {
	basePath: "/",
	fragilityService: "http://localhost:8080/fragility/api/fragilities",
	semanticService: "",
	maestroService: "http://localhost:8080",
	dataService: "http://141.142.210.193:8888/data/api/datasets",
	dataWolf: "http://141.142.209.63/datawolf/"
};

const prodConfig = {
	basePath: "/web/",
	fragilityService: "",
	semanticService: "",
	maestroService: ""
};

const config = getConfig();

function getConfig() {
	if (process.env.NODE_ENV === "production") {
		return prodConfig;
	} else {
		return devConfig;
	}
}

export default config;
