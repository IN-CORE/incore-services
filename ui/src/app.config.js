const devConfig = {
	basePath: "/",
	fragilityServer: "",
	fragilityMappingServer: "",
	semanticServer: ""
};

const prodConfig = {
	basePath: "/incore/",
	fragilityServer: "",
	fragilityMappingServer: "",
	semanticServer: ""
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
