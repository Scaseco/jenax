const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');

module.exports = {
  entry: {
    graphql: './src/graphql/mui/index.js',
    yasgui: './src/yasgui/index.js'
  },
  output: {
    path: path.resolve(__dirname, 'build'),
    filename: '[name].bundle.js'
  },
  module: {
    rules: [
    /*
      {
        test: /\.css$/,
        use: ['style-loader', 'css-loader']
      },
      */
      {
        test: /\.css$/,
        use: [ MiniCssExtractPlugin.loader, 'style-loader', 'css-loader' ],
      },      
      {
        test: /\.html$/,
        use: 'html-loader'
      },
      {
        test: /\.js$/,
        exclude: /node_modules/, // Exclude node_modules from transpiling
        use: {
          loader: 'babel-loader', // Use Babel to transpile modern JavaScript
          options: {
            presets: ['@babel/preset-env'], // Use preset-env for ES6+ features
          },
        },
      },
      {
        test: /\.scss$/,
        exclude: /node_modules/,
        use: ['style-loader', 'css-loader', 'sass-loader'],
      },
    ]
  },
  plugins: [
    new CopyWebpackPlugin({
      patterns: [
        { from: './src/index.html', to: 'index.html' },
        { from: './src/style.css', to: 'style.css' },
        { from: './src/logo', to: 'logo' },
        { from: './src/snorql', to: 'snorql' },
        { from: './src/view', to: 'view' },
        { from: './src/graph-explorer', to: 'graph-explorer' },
      ]
    }),
    new HtmlWebpackPlugin({
      template: './src/graphql/mui/index.html',
      filename: 'graphql/mui/index.html',
      chunks: ['graphql']
    }),
    new HtmlWebpackPlugin({
      template: './src/yasgui/index.html',
      filename: 'yasgui/index.html',
      chunks: ['yasgui']
    }),
    new MiniCssExtractPlugin({
      filename: '[name].css',
    })
  ],
  mode: 'production'
};

