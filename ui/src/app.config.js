const devConfig = {
	basePath: "/",
	fragilityService: "http://localhost:8080/api/fragilities",
	semanticService: "",
	maestroService: ""
};

const prodConfig = {
	basePath: "/incore/",
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
