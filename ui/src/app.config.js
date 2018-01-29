const devConfig = {
	basePath: "/",
	fragilityService: "http://localhost:8088/fragility/api/fragilities",
	semanticService: "",
	hazardService: "http://localhost:8088/hazard/api/earthquakes/",
	maestroService: "http://localhost:8088/maestro",
	authService: "http://localhost:8088/auth/api/login",
	dataServiceBase: "http://localhost:8088/",
	dataService: "http://localhost:8088/data/api/datasets",
	dataWolf: "https://localhost:8088/datawolf/"
};

const prodConfig = {
	basePath: "/",
	fragilityService: "https://incore2-services.ncsa.illinois.edu/fragility/api/fragilities",
	semanticService: "",
	hazardService: "https://incore2-services.ncsa.illinois.edu/hazard/api/earthquakes/",
	maestroService: "https://incore2-services.ncsa.illinois.edu/maestro",
	authService: "https://incore2-services.ncsa.illinois.edu/auth/api/login",
	dataServiceBase: "https://incore2-services.ncsa.illinois.edu/",
	dataService: "https://incore2-services.ncsa.illinois.edu/data/api/datasets",
	dataWolf: "https://incore2-datawolf.ncsa.illinois.edu/datawolf/"
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
