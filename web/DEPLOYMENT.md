# Attorney Mode Jurisprudence Framework - Deployment Guide

## Overview

This guide provides instructions for deploying the MONTI Attorney Mode Jurisprudence Framework web application.

## Prerequisites

- Node.js 16 or higher
- npm or yarn package manager
- A web server or hosting platform

## Local Development

### Installation

1. Navigate to the web directory:
```bash
cd web
```

2. Install dependencies:
```bash
npm install
```

### Running Development Server

Start the development server with hot-reload:
```bash
npm run dev
```

The application will be available at `http://localhost:5173`

### Building for Production

Build the application for production deployment:
```bash
npm run build
```

The optimized files will be generated in the `dist` directory.

### Preview Production Build

Preview the production build locally:
```bash
npm run preview
```

## Deployment Options

### Static Hosting (Recommended)

The application is a static web app and can be deployed to any static hosting service:

#### Netlify
1. Connect your repository to Netlify
2. Set build command: `cd web && npm install && npm run build`
3. Set publish directory: `web/dist`

#### Vercel
1. Import your repository to Vercel
2. Set root directory: `web`
3. Build command will be auto-detected
4. Deploy

#### GitHub Pages
1. Build the application:
```bash
cd web && npm run build
```

2. Deploy the `dist` folder to GitHub Pages

#### AWS S3 + CloudFront
1. Build the application
2. Upload `dist` folder contents to S3 bucket
3. Configure CloudFront distribution
4. Set index.html as the default root object

### Traditional Web Server

#### Apache
1. Build the application
2. Copy `dist` folder contents to Apache document root
3. Configure `.htaccess` for single-page application routing:
```apache
<IfModule mod_rewrite.c>
  RewriteEngine On
  RewriteBase /
  RewriteRule ^index\.html$ - [L]
  RewriteCond %{REQUEST_FILENAME} !-f
  RewriteCond %{REQUEST_FILENAME} !-d
  RewriteRule . /index.html [L]
</IfModule>
```

#### Nginx
1. Build the application
2. Copy `dist` folder contents to Nginx document root
3. Configure nginx.conf:
```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

### Docker Deployment

Create a `Dockerfile` in the web directory:

```dockerfile
FROM node:18-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

Build and run:
```bash
docker build -t attorney-mode-framework .
docker run -p 80:80 attorney-mode-framework
```

## Environment Configuration

The application currently uses no environment variables. If you need to add API endpoints or configuration:

1. Create `.env` file in the web directory:
```bash
VITE_API_URL=https://api.attorneymode.com
VITE_EVIDENCE_MANAGER=https://evidence.attorneymode.com
```

2. Access in TypeScript code:
```typescript
const apiUrl = import.meta.env.VITE_API_URL;
```

## Monitoring and Maintenance

### Health Checks

The application provides visual indicators for:
- Network Health (98.7% normal operating range)
- Evidence Queue status
- Threat detection status
- INSPECTRUM security status

### Performance Optimization

The application is already optimized for production:
- ✅ Code splitting with Vite
- ✅ Tree shaking for minimal bundle size
- ✅ CSS minification
- ✅ Asset optimization

### Browser Support

The application supports all modern browsers:
- Chrome/Edge 90+
- Firefox 88+
- Safari 14+

## Security Considerations

### Content Security Policy

Consider adding these headers to your web server:

```
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
```

### HTTPS

Always serve the application over HTTPS in production environments.

## Troubleshooting

### Build Fails

If build fails with TypeScript errors:
```bash
npm run build -- --mode development
```

### Development Server Issues

Clear cache and reinstall:
```bash
rm -rf node_modules package-lock.json
npm install
```

### Production 404 Errors

Ensure your web server is configured to serve index.html for all routes (SPA routing).

## Support

For issues and questions:
- GitHub Repository: montinode/TTAG
- Evidence Manager: attorneymode.com
- Network Protocol: montinode.com/RfcAI

## License

Part of the TTAG (TelemetricTelephonyAutomationGeospatialAdministrationTektronicTracer) system.
