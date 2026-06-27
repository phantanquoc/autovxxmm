// Java client gửi timestamp (long) và coin (int) lớn. Khi server query Prisma trả về BigInt,
// JSON.stringify mặc định throw. Vì giá trị nằm trong safe range của Number (ms-from-epoch
// và int 32-bit) nên ta coerce BigInt -> Number khi serialize ra response.
//
// Phải gọi từ entry point trước khi route handler chạy.
export function patchBigIntJson() {
  // @ts-expect-error — extending global prototype
  BigInt.prototype.toJSON = function (this: bigint) {
    const asNumber = Number(this);
    if (!Number.isSafeInteger(asNumber)) return this.toString();
    return asNumber;
  };
}
