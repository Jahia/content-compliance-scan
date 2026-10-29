const path = require('path');
// The package ships no index.js: require the file directly.
const getModuleFederationConfig = require('@jahia/webpack-config/getModuleFederationConfig');
const {CleanWebpackPlugin} = require('clean-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const ModuleFederationPlugin = require('webpack/lib/container/ModuleFederationPlugin');
const packageJson = require('./package.json');

module.exports = (env, argv) => {
    const isProduction = argv.mode === 'production';
    return {
        entry: {main: path.resolve(__dirname, 'src/javascript/index')},
        output: {
            path: path.resolve(__dirname, 'src/main/resources/javascript/apps/'),
            publicPath: 'auto',
            filename: 'content-compliance-scan.bundle.js',
            chunkFilename: '[name].content-compliance-scan.[chunkhash:6].js'
        },
        resolve: {extensions: ['.mjs', '.js', '.jsx']},
        module: {
            rules: [
                {test: /\.jsx?$/, use: 'babel-loader', exclude: /node_modules/},
                {
                    test: /\.module\.css$/,
                    // css-loader 6 (pinned): modules default to a default export. css-loader 7 switches to
                    // named exports and needs {namedExport: false, exportLocalsConvention: 'as-is'}.
                    use: ['style-loader', {loader: 'css-loader', options: {modules: true}}]
                },
                {test: /\.css$/, exclude: /\.module\.css$/, use: ['style-loader', 'css-loader']}
            ]
        },
        plugins: [
            new ModuleFederationPlugin(getModuleFederationConfig(packageJson, {
                name: 'contentComplianceScan',
                library: {type: 'assign', name: 'appShell.remotes.contentComplianceScan'},
                filename: 'remoteEntry.js',
                exposes: {
                    './init': './src/javascript/init'
                },
                remotes: {
                    '@jahia/app-shell': 'appShellRemote',
                    '@jahia/jcontent': 'appShell.remotes.jcontent'
                },
                shared: {
                    react: {singleton: true, requiredVersion: packageJson.dependencies.react},
                    'react-dom': {singleton: true, requiredVersion: packageJson.dependencies['react-dom']},
                    // Host-provided, never bundled.
                    'react-redux': {singleton: true, import: false},
                    redux: {singleton: true, import: false},
                    '@apollo/client': {singleton: true, import: false}
                }
            })),
            new CleanWebpackPlugin({verbose: false}),
            // app-shell discovers the remote through the "jahia.remotes" key of this file.
            new CopyWebpackPlugin({patterns: [{from: './package.json', to: ''}]})
        ],
        mode: isProduction ? 'production' : 'development',
        devtool: isProduction ? 'source-map' : 'eval-source-map'
    };
};
