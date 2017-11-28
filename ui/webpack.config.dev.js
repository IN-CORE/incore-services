import webpack from "webpack";
import HtmlWebpackPlugin from "html-webpack-plugin";
import autoprefixer from "autoprefixer";
import path from "path";

export default {
	resolve: {
		extensions: [".js", ".jsx", ".json"]
	},
	devtool: "source-map",
	entry: [
		"babel-polyfill",
		"whatwg-fetch",
		"./src/webpack-public-path",
		"webpack-hot-middleware/client?reload=true",
		path.resolve(__dirname, "src/index.js")
	],
	target: "web",
	output: {
		path: path.resolve(__dirname, "dist"),
		publicPath: "/",
		filename: "bundle.js"
	},
	plugins: [
		new webpack.DefinePlugin({
			"process.env.NODE_ENV": JSON.stringify("development"),
			__DEV__: true
		}),
		new webpack.HotModuleReplacementPlugin(),
		new webpack.NoEmitOnErrorsPlugin(),
		new HtmlWebpackPlugin({
			template: "src/index.ejs",
			minify: {
				removeComments: true,
				collapseWhitespace: true
			},
			inject: true
		}),
		new webpack.LoaderOptionsPlugin({
			debug: true,
			options: {
				sassLoader: {
					includePaths: [path.resolve(__dirname, "src", "scss")]
				},
				context: "/",
				postcss: [
					autoprefixer(),
				]
			}
		})
	],
	module: {
		loaders: [
			{test: /\.jsx?$/, exclude: /node_modules/, loaders: ["babel-loader"]},
			{test: /\.eot(\?v=\d+.\d+.\d+)?$/, loader: "file-loader"},
			{
				test: /\.woff(2)?(\?v=[0-9]\.[0-9]\.[0-9])?$/,
				loader: "url-loader?limit=10000&mimetype=application/font-woff"
			},
			{test: /\.[ot]tf(\?v=\d+.\d+.\d+)?$/, loader: "url-loader?limit=10000&mimetype=application/octet-stream"},
			{test: /\.svg(\?v=\d+\.\d+\.\d+)?$/, loader: "url-loader?limit=10000&mimetype=image/svg+xml"},
			{test: /\.(jpe?g|png|gif)$/i, loader: "file-loader?name=[name].[ext]"},
			{test: /\.ico$/, loader: "file-loader?name=[name].[ext]"},
			{
				test: /(\.css|\.scss)$/,
				loaders: ["style-loader", "css-loader?sourceMap", "postcss-loader", "sass-loader?sourceMap"]
			},
			{test: /\.json$/, loader: "json-loader"}
		]
	}
};
