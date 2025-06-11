const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');

module.exports = {
  entry: {
    'graphql/mui/graphql': './src/graphql/mui/index.js'
  },
  output: {
    path: path.resolve(__dirname, 'build'),
    // publicPath: '',
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
      }
    ]
  },
  plugins: [
    new CopyWebpackPlugin({
      patterns: [
        { from: './src/index.html', to: 'index.html' },
        { from: './src/style.css', to: 'style.css' },
      ]
    }),
    new HtmlWebpackPlugin({
      template: './src/graphql/mui/index.ejs',
      filename: 'graphql/mui/index.html',
      chunks: [ 'graphql/mui/graphql' ],
      inject: false, // turn off automatic script injection
      // scriptLoading: 'blocking', // optional, ensures scripts are blocking
      templateParameters: {
        scriptSrc: 'graphql.bundle.js', // relative to HTML
      }
    }),
    new MiniCssExtractPlugin({
      filename: '[name].css',
    })
  ],
  // mode: 'development'
  mode: 'production',
  optimization: {
    minimize: false // This disables minification
  }
};

