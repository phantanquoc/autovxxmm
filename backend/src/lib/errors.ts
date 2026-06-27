export class HttpError extends Error {
  constructor(public status: number, message: string, public data?: unknown) {
    super(message);
  }
}

export const unauthorized = (msg = 'Unauthorized') => new HttpError(401, msg);
export const badRequest = (msg: string, data?: unknown) => new HttpError(400, msg, data);
export const notFound = (msg = 'Not found') => new HttpError(404, msg);
export const forbidden = (msg = 'Forbidden') => new HttpError(403, msg);
