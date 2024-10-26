FROM nginx:latest

WORKDIR /usr/share/nginx/html

COPY welcome/login_interface /usr/share/nginx/html/login_interface
COPY compiler-engine/compiler_interface /usr/share/nginx/html/compiler_interface

EXPOSE 80
