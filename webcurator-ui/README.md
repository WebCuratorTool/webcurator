# webcurator-ui

This template should help get you started developing with Vue 3 in Vite.

## Development Environment Setup

### Operating System and Nodejs
Ubuntu20.04 or newer is recommended for development. Nodejs is a JavaScript runtime for programming. In this guide, we will show you how to install the proper Nodejs on Ubuntu20.04.

First, install the PPA to get access to its packages. Use curl to retrieve the installation script for your preferred version. For vue3, the version of Nodejs should be 20+.
```sh
curl -sL https://deb.nodesource.com/setup_20.x -o /tmp/nodesource_setup.sh
```

Run the script with sudo. The PPA will be added to your configuration and your local package cache will be updated automatically.
```sh
sudo bash /tmp/nodesource_setup.sh
```

Run the apt install with sudo.
```sh
sudo apt install nodejs
```

Verify that you’ve installed the new version by running node and npm with the -v version flag:
```sh
node -v
npm -v
```

It should output the version number like this:
```sh
Output for: node -v
v20.19.1

Output for: npm -v
10.8.2
```

### Recommended IDE Setup

[VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur).

## Type Support for `.vue` Imports in TS

TypeScript cannot handle type information for `.vue` imports by default, so we replace the `tsc` CLI with `vue-tsc` for type checking. In editors, we need [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) to make the TypeScript language service aware of `.vue` types.

## Customize configuration

See [Vite Configuration Reference](https://vitejs.dev/config/).

## Project Setup

```sh
npm install
```

### Compile and Hot-Reload for Development

```sh
npm run dev
```

### Type-Check, Compile and Minify for Production

```sh
npm run build
```

### Run Unit Tests with [Vitest](https://vitest.dev/)

```sh
npm run test:unit
```

### Lint with [ESLint](https://eslint.org/)

```sh
npm run lint
```

### Deploy in production environment
The webcurator-ui has been integrated to webcurator-webapp and served by webcurator-webapp. When you build the webcurator-webapp project, the webcurator-ui will be packaged into the war file automatically.

Then you can build and run webcurator-webapp. The access url of the new ui: http://localhost:8080/wct/index.html.

A link is put in the existing ui for the benefit of new ui developers. You can login into http://localhost:8080/wct/curator/home.html, and click the "New UI" link on the top right to open the new ui.

