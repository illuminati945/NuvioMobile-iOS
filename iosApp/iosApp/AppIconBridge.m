#import <UIKit/UIKit.h>
#include <stdbool.h>

typedef void (*NuvioAppIconCompletion)(bool);

bool NuvioSupportsAlternateAppIcons(void) {
    return UIApplication.sharedApplication.supportsAlternateIcons;
}

bool NuvioIsCurrentAlternateAppIcon(const char *name) {
    NSString *currentName = UIApplication.sharedApplication.alternateIconName;
    if (name == NULL) {
        return currentName == nil;
    }
    return [currentName isEqualToString:[NSString stringWithUTF8String:name]];
}

void NuvioSetAlternateAppIconName(const char *name, NuvioAppIconCompletion completion) {
    NSString *iconName = name == NULL ? nil : [NSString stringWithUTF8String:name];
    void (^changeIcon)(void) = ^{
        UIApplication *application = UIApplication.sharedApplication;
        if (!application.supportsAlternateIcons) {
            if (completion != NULL) {
                completion(false);
            }
            return;
        }
        [application setAlternateIconName:iconName completionHandler:^(NSError *error) {
            dispatch_async(dispatch_get_main_queue(), ^{
                if (completion != NULL) {
                    completion(error == nil);
                }
            });
        }];
    };
    if (NSThread.isMainThread) {
        changeIcon();
    } else {
        dispatch_async(dispatch_get_main_queue(), changeIcon);
    }
}

#pragma mark - UIViewLayoutRegion Compatibility Layer for Compose Multiplatform

#import <objc/runtime.h>
#import <objc/message.h>

typedef NS_ENUM(NSInteger, UIViewLayoutRegionAdaptivityAxis) {
    UIViewLayoutRegionAdaptivityAxisNone = 0,
    UIViewLayoutRegionAdaptivityAxisHorizontal = 1,
    UIViewLayoutRegionAdaptivityAxisVertical = 2,
    UIViewLayoutRegionAdaptivityAxisBoth = 3,
};

typedef NS_ENUM(NSInteger, UIViewLayoutRegionType) {
    UIViewLayoutRegionTypeSafeArea = 0,
    UIViewLayoutRegionTypeMargins = 1,
    UIViewLayoutRegionTypeReadableContent = 2,
};

__attribute__((visibility("default")))
@interface UIViewLayoutRegion : NSObject
@property (nonatomic, assign) UIViewLayoutRegionAdaptivityAxis adaptivityAxis;
@property (nonatomic, assign) UIViewLayoutRegionType regionType;

+ (instancetype)marginsLayoutRegionWithCornerAdaptation:(UIViewLayoutRegionAdaptivityAxis)axis;
+ (instancetype)readableContentLayoutRegionWithCornerAdaptation:(UIViewLayoutRegionAdaptivityAxis)axis;
+ (instancetype)safeAreaLayoutRegionWithCornerAdaptation:(UIViewLayoutRegionAdaptivityAxis)axis;
+ (instancetype)layoutRegion;
+ (instancetype)marginsLayoutRegion;
+ (instancetype)readableContentLayoutRegion;
+ (instancetype)safeAreaLayoutRegion;
- (UIEdgeInsets)edgeInsetsInView:(UIView *)view;
- (UIEdgeInsets)edgeInsetsForView:(UIView *)view;
@end

static id dynamicLayoutRegionClassIMP(id self, SEL _cmd, ...) {
    return [[UIViewLayoutRegion alloc] init];
}

static id dynamicLayoutRegionInstanceIMP(id self, SEL _cmd, ...) {
    return nil;
}

@implementation UIViewLayoutRegion

+ (instancetype)marginsLayoutRegionWithCornerAdaptation:(UIViewLayoutRegionAdaptivityAxis)axis {
    UIViewLayoutRegion *r = [[self alloc] init];
    r.adaptivityAxis = axis;
    r.regionType = UIViewLayoutRegionTypeMargins;
    return r;
}

+ (instancetype)readableContentLayoutRegionWithCornerAdaptation:(UIViewLayoutRegionAdaptivityAxis)axis {
    UIViewLayoutRegion *r = [[self alloc] init];
    r.adaptivityAxis = axis;
    r.regionType = UIViewLayoutRegionTypeReadableContent;
    return r;
}

+ (instancetype)safeAreaLayoutRegionWithCornerAdaptation:(UIViewLayoutRegionAdaptivityAxis)axis {
    UIViewLayoutRegion *r = [[self alloc] init];
    r.adaptivityAxis = axis;
    r.regionType = UIViewLayoutRegionTypeSafeArea;
    return r;
}

+ (instancetype)layoutRegion {
    return [[self alloc] init];
}

+ (instancetype)marginsLayoutRegion {
    return [self marginsLayoutRegionWithCornerAdaptation:UIViewLayoutRegionAdaptivityAxisNone];
}

+ (instancetype)readableContentLayoutRegion {
    return [self readableContentLayoutRegionWithCornerAdaptation:UIViewLayoutRegionAdaptivityAxisNone];
}

+ (instancetype)safeAreaLayoutRegion {
    return [self safeAreaLayoutRegionWithCornerAdaptation:UIViewLayoutRegionAdaptivityAxisNone];
}

- (UIEdgeInsets)edgeInsetsInView:(UIView *)view {
    if (!view || ![view isKindOfClass:[UIView class]]) {
        return UIEdgeInsetsZero;
    }
    if (self.regionType == UIViewLayoutRegionTypeMargins) {
        return view.layoutMargins;
    }
    return view.safeAreaInsets;
}

- (UIEdgeInsets)edgeInsetsForView:(UIView *)view {
    return [self edgeInsetsInView:view];
}

+ (BOOL)resolveClassMethod:(SEL)sel {
    Class metaClass = object_getClass(self);
    class_addMethod(metaClass, sel, (IMP)dynamicLayoutRegionClassIMP, "@@:@");
    return YES;
}

+ (BOOL)resolveInstanceMethod:(SEL)sel {
    class_addMethod(self, sel, (IMP)dynamicLayoutRegionInstanceIMP, "@@:@");
    return YES;
}

+ (NSMethodSignature *)methodSignatureForSelector:(SEL)aSelector {
    return [NSMethodSignature signatureWithObjCTypes:"@@:@"];
}

+ (void)forwardInvocation:(NSInvocation *)anInvocation {
    id result = [[self alloc] init];
    [anInvocation setReturnValue:&result];
}

- (NSMethodSignature *)methodSignatureForSelector:(SEL)aSelector {
    return [NSMethodSignature signatureWithObjCTypes:"@@:@"];
}

- (void)forwardInvocation:(NSInvocation *)anInvocation {
    id result = nil;
    [anInvocation setReturnValue:&result];
}

@end

#pragma mark - UIView Layout Region Category

@interface UIView (UIViewLayoutRegionSupport)
- (UIEdgeInsets)edgeInsetsForLayoutRegion:(id)region;
- (UIEdgeInsets)_edgeInsetsForLayoutRegion:(id)region;
- (UILayoutGuide *)layoutGuideForLayoutRegion:(id)region;
@end

@implementation UIView (UIViewLayoutRegionSupport)

- (UIEdgeInsets)edgeInsetsForLayoutRegion:(id)region {
    if (region && [region respondsToSelector:@selector(regionType)]) {
        NSInteger type = [(UIViewLayoutRegion *)region regionType];
        if (type == UIViewLayoutRegionTypeMargins) {
            return self.layoutMargins;
        }
    }
    return self.safeAreaInsets;
}

- (UIEdgeInsets)_edgeInsetsForLayoutRegion:(id)region {
    return [self edgeInsetsForLayoutRegion:region];
}

- (UILayoutGuide *)layoutGuideForLayoutRegion:(id)region {
    if (region && [region respondsToSelector:@selector(regionType)]) {
        NSInteger type = [(UIViewLayoutRegion *)region regionType];
        if (type == UIViewLayoutRegionTypeMargins) {
            return self.layoutMarginsGuide;
        } else if (type == UIViewLayoutRegionTypeReadableContent) {
            return self.readableContentGuide;
        }
    }
    return self.safeAreaLayoutGuide;
}

@end


